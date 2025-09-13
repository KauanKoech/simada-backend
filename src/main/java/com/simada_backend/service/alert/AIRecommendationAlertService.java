package com.simada_backend.service.alert;

import com.simada_backend.dto.request.alert.PerfAlertRecoRequest;
import com.simada_backend.dto.request.alert.PsyAlertRecoRequest;
import com.simada_backend.model.recommendation.PerfRecommendation;
import com.simada_backend.model.recommendation.PsyRecommendation;
import com.simada_backend.integrations.GroqClient;
import com.simada_backend.repository.recommendation.PerfRecommendationRepository;
import com.simada_backend.repository.recommendation.PsyRecommendationRepository;
import com.simada_backend.repository.session.SessionRepository;
import com.simada_backend.utils.Labels;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AIRecommendationAlertService {

    private final GroqClient groqClient;
    private final PsyRecommendationRepository psyRecommendationRepository;
    private final PerfRecommendationRepository perfRecommendationRepository;
    private final SessionRepository sessionRepository;

    @Value("${groq.model:}")
    private String modelConfigured;

    @Transactional(readOnly = true)
    public Mono<String> generatePsychoRecommendations(Long sessionId, Long athleteId, PsyAlertRecoRequest req) {
        return Mono.fromCallable(() ->
                        psyRecommendationRepository.findBySessionIdAndAthleteId(sessionId, athleteId)
                )
                .flatMap(opt -> opt.map(rec -> Mono.just(rec.getText()))
                        .orElseGet(() -> psychoCallGroqAndPersist(sessionId, athleteId, req)));
    }

    @Transactional(readOnly = true)
    public Mono<String> generatePerformanceRecommendations(Long sessionId, Long athleteId, PerfAlertRecoRequest req) {
        return Mono.fromCallable(() ->
                        perfRecommendationRepository.findBySessionIdAndAthleteId(sessionId, athleteId)
                )
                .flatMap(opt -> opt.map(rec -> Mono.just(rec.getText()))
                        .orElseGet(() -> perfCallGroqAndPersist(sessionId, athleteId, req)));
    }

    private Mono<String> psychoCallGroqAndPersist(Long sessionId, Long athleteId, PsyAlertRecoRequest req) {
        Long foundCoachId = sessionRepository.findCoachIdBySessionId(sessionId);
        final Long finalCoachId = (foundCoachId != null ? foundCoachId : 0L);

        String systemPrompt = """
                You are a sports performance assistant. Generate concise, clear, and actionable recommendations
                for a HUMAN athlete, focusing on recovery and psycho-emotional well-being, based on scores 0–10:
                - sRPE (0 very light, 10 extreme)
                - Fatigue (0 no fatigue, 10 exhausted)
                - Soreness (0 no soreness, 10 very sore)
                - Mood (0 very bad, 10 very good)
                - Energy (0 very low, 10 very high)
                
                Rules:
                - Output format: markdown with 3–7 concise bullets (each 1–2 sentences).
                - Language: English, pragmatic and empathetic tone.
                - Do not make medical diagnoses; just highlight recovery strategies and when to seek help.
                - If scores indicate risk (e.g., mood ≤ 3, energy ≤ 3, fatigue ≥ 7, soreness ≥ 7), include advice on reduced training load, sleep, hydration, nutrition, and recovery strategies.
                - Always consider sRPE as an indicator of perceived session load.
                - IMPORTANT: Always end the text with one final line:
                  "⚠️ Please seek professional medical support if symptoms persist or worsen."
                """;

        String userPrompt = """
                Session/Athlete context:
                - sessionId: %d
                - athleteId: %d
                
                Scores (0–10):
                - sRPE: %d
                - Fatigue: %d
                - Soreness: %d
                - Mood: %d
                - Energy: %d
                
                Expected output:
                - Markdown list of 3–7 practical bullets (1–2 sentences each).
                - The last line must always be:
                  "⚠️ Please seek professional medical support if symptoms persist or worsen."
                """.formatted(
                sessionId, athleteId,
                req.srpe(), req.fatigue(), req.soreness(), req.mood(), req.energy()
        );

        return groqClient.chat(systemPrompt, userPrompt)
                // Se vier vazio/nulo, considera erro para acionar onErrorResume -> fallback
                .flatMap(text -> (text != null && !text.isBlank())
                        ? Mono.just(saveAndReturnPsychoRec(sessionId, athleteId, finalCoachId, req, text, PsyRecommendation.Source.groq))
                        : Mono.error(new IllegalStateException("Empty response from Groq")))
                // Erros (timeout, 4xx/5xx, vazio) -> fallback
                .onErrorResume(ex -> {
                    String fb = psyFallback(req);
                    return Mono.just(saveAndReturnPsychoRec(sessionId, athleteId, finalCoachId, req, fb, PsyRecommendation.Source.fallback));
                });
    }

    private Mono<String> perfCallGroqAndPersist(Long sessionId, Long athleteId, PerfAlertRecoRequest req) {
        Long foundCoachId = sessionRepository.findCoachIdBySessionId(sessionId);
        final Long finalCoachId = (foundCoachId != null ? foundCoachId : 0L);


        String acwrLabel = Labels.acwrLabel(d(req.acwr()));
        String pctQwLabel = Labels.pctQwUpLabel(d(req.pctQwUp()));
        String monoLabel = Labels.monotonyLabel(d(req.monotony()));
        String strainLabel = Labels.strainLabel(d(req.strain()));

        String perfSystemPrompt = """
                You are a sports performance assistant. Generate concise, clear, and actionable recommendations
                for TRAINING LOAD MANAGEMENT, organized BY METRIC. The four metrics are:
                - ACWR (acute:chronic workload ratio)
                - Weekly Load Change (%%↑QW vs previous week)
                - Monotony (Foster)
                - Strain (Foster)
                
                Output rules:
                - Markdown, with one subsection per metric that is present:
                  - "### ACWR", "### Weekly Load Change (%%↑QW)", "### Monotony", "### Strain"
                  - Under each subsection: 2–3 bullets (1–2 sentences each) tailored to that metric's value/label.
                - Language: English, pragmatic and empathetic tone.
                - Focus on concrete next-session and next-week actions (load adjustments, distribution, recovery hygiene, monitoring).
                - Do not make medical diagnoses.
                - IMPORTANT: Always end with this final line:
                  "Please seek professional medical support if symptoms persist or worsen."
                
                Labels guide (use only to guide advice, do not restate thresholds):
                - ACWR labels: baixo, ótimo, atenção, risco.
                - %%↑QW labels: queda_forte, estável, atenção, risco.
                - Monotony labels: saudável, atenção, alto_risco.
                - Strain labels: baixo, atenção, alto_risco.
                """;


        String perfUserPrompt = """
                Session/Athlete context:
                - sessionId: %d
                - athleteId: %d
                
                Metrics (value + label):
                - ACWR: %s (%s)
                - Weekly Load Change (%%↑QW): %s%% (%s)
                - Monotony: %s (%s)
                - Strain: %s (%s)
                
                Expected output:
                - Markdown sections: "### ACWR", "### Weekly Load Change (%%↑QW)", "### Monotony", "### Strain".
                - For each present metric: 2–3 concise bullets with practical actions (next session & next week).
                - End with: "⚠️ Please seek professional medical support if symptoms persist or worsen."
                """.formatted(
                sessionId,
                athleteId,
                req.acwr(), acwrLabel,
                req.pctQwUp(), pctQwLabel,
                req.monotony(), monoLabel,
                req.strain(), strainLabel
        );


        return groqClient.chat(perfSystemPrompt, perfUserPrompt)
                // Se vier vazio/nulo, considera erro para acionar onErrorResume -> fallback
                .flatMap(text -> (text != null && !text.isBlank())
                        ? Mono.just(saveAndReturnPerfRec(sessionId, athleteId, finalCoachId, req, text, PerfRecommendation.Source.groq))
                        : Mono.error(new IllegalStateException("Empty response from Groq")))
                // Erros (timeout, 4xx/5xx, vazio) -> fallback
                .onErrorResume(ex -> {
                    String fb = perfFallback(req);
                    return Mono.just(saveAndReturnPerfRec(sessionId, athleteId, finalCoachId, req, fb, PerfRecommendation.Source.fallback));
                });
    }

    private String saveAndReturnPsychoRec(Long sessionId, Long athleteId, Long coachId,
                                          PsyAlertRecoRequest req, String text,
                                          com.simada_backend.model.recommendation.PsyRecommendation.Source source) {
        PsyRecommendation rec = PsyRecommendation.builder()
                .sessionId(sessionId)
                .athleteId(athleteId)
                .coachId(coachId)
                .text(text)
                .lang("en")
                .model(source == PsyRecommendation.Source.groq ? modelConfigured : null)
                .source(source)
                .srpe(req.srpe())
                .fatigue(req.fatigue())
                .soreness(req.soreness())
                .mood(req.mood())
                .energy(req.energy())
                .createdAt(Instant.now())
                .build();
        return saveBlockingPsycho(rec).getText();
    }

    private String saveAndReturnPerfRec(Long sessionId, Long athleteId, Long coachId,
                                        PerfAlertRecoRequest req, String text,
                                        com.simada_backend.model.recommendation.PerfRecommendation.Source source) {
        PerfRecommendation rec = PerfRecommendation.builder()
                .sessionId(sessionId)
                .athleteId(athleteId)
                .coachId(coachId)
                .text(text)
                .lang("en")
                .model(source == PerfRecommendation.Source.groq ? modelConfigured : null)
                .source(source)
                .acwr(req.acwr())
                .monotony(req.monotony())
                .strain(req.strain())
                .pctQwUp(req.pctQwUp())
                .createdAt(Instant.now())
                .build();
        return saveBlockingPerf(rec).getText();
    }

    @Transactional
    protected PsyRecommendation saveBlockingPsycho(PsyRecommendation rec) {
        return psyRecommendationRepository.findBySessionIdAndAthleteId(rec.getSessionId(), rec.getAthleteId())
                .orElseGet(() -> {
                    try {
                        return psyRecommendationRepository.save(rec);
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // em caso de corrida na UNIQUE(session_id, athlete_id), retorna o já existente
                        return psyRecommendationRepository
                                .findBySessionIdAndAthleteId(rec.getSessionId(), rec.getAthleteId())
                                .orElseThrow(() -> e);
                    }
                });
    }

    @Transactional
    protected PerfRecommendation saveBlockingPerf(PerfRecommendation rec) {
        return perfRecommendationRepository.findBySessionIdAndAthleteId(rec.getSessionId(), rec.getAthleteId())
                .orElseGet(() -> {
                    try {
                        return perfRecommendationRepository.save(rec);
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // em caso de corrida na UNIQUE(session_id, athlete_id), retorna o já existente
                        return perfRecommendationRepository
                                .findBySessionIdAndAthleteId(rec.getSessionId(), rec.getAthleteId())
                                .orElseThrow(() -> e);
                    }
                });
    }

    private String psyFallback(PsyAlertRecoRequest r) {
        int riskCount = 0;
        boolean highFatigue = r.fatigue() >= 7;
        if (highFatigue) riskCount++;
        boolean highSoreness = r.soreness() >= 7;
        if (highSoreness) riskCount++;
        boolean lowMood = r.mood() <= 3;
        if (lowMood) riskCount++;
        boolean lowEnergy = r.energy() <= 3;
        if (lowEnergy) riskCount++;
        boolean highSRPE = r.srpe() >= 7;
        if (highSRPE) riskCount++;

        StringBuilder sb = new StringBuilder();
        sb.append("**Recommendations (fallback):**\n");
        sb.append("- Prioritize **sleep** (7–9h) and **hydration** throughout the day.\n");
        if (highSRPE || highFatigue)
            sb.append("- Reduce **training load** in the next session (volume/intensity) and include a block of **active recovery**.\n");
        if (highSoreness)
            sb.append("- Add **light mobility**, **contrast baths**, or **compression**; avoid load peaks on sore muscle groups.\n");
        if (lowEnergy)
            sb.append("- Have **balanced meals** (carbs + protein) within 2–3h post-session; consider consulting a nutritionist/doctor for micronutrients.\n");
        if (lowMood)
            sb.append("- Practice **breathing/relaxation** for 5–10 minutes; align expectations with the coach and track stress triggers.\n");
        if (riskCount >= 2)
            sb.append("- Consider **daily monitoring** for the next 2–3 days (RPE, sleep, mood) and apply progressive load adjustments.\n");
        sb.append("\n⚠️ Please seek professional medical support if symptoms persist or worsen.\n");
        return sb.toString();
    }

    private String perfFallback(PerfAlertRecoRequest r) {
        Double acwrD = r.acwr() == null ? null : r.acwr().doubleValue();
        Double pctD = r.pctQwUp() == null ? null : r.pctQwUp().doubleValue();
        Double monoD = r.monotony() == null ? null : r.monotony().doubleValue();
        Double strainD = r.strain() == null ? null : r.strain().doubleValue();

        String acwrL = Labels.acwrLabel(acwrD);
        String pctL = Labels.pctQwUpLabel(pctD);
        String monoL = Labels.monotonyLabel(monoD);
        String strainL = Labels.strainLabel(strainD);

        StringBuilder sb = new StringBuilder();
        sb.append("**Recommendations (fallback):**\n\n");

        // ACWR
        if (r.acwr() != null) {
            sb.append("### ACWR\n");
            switch (acwrL) {
                case "risco" -> {
                    sb.append("- Reduce **next-week volume by 10–20%**, cap high-intensity exposures, and avoid single-session load peaks.\n");
                    sb.append("- Distribute intensity more evenly across the week; consider an **easy/recovery day**.\n");
                }
                case "atenção" -> {
                    sb.append("- Keep **weekly increments <10%** and avoid adding extra conditioning blocks this week.\n");
                    sb.append("- Monitor daily wellness/RPE and schedule one **lower-load session** to buffer accumulation.\n");
                }
                case "baixo" -> {
                    sb.append("- **Rebuild gradually**: increase total load by 5–10% next week with emphasis on technical quality.\n");
                    sb.append("- Maintain at least one **moderate session** to progress toward the optimal range.\n");
                }
                default /* ótimo / indisponível */ -> {
                    sb.append("- Maintain **stable progression** (<10%); keep balanced intensity distribution.\n");
                    sb.append("- Continue daily recovery hygiene (sleep, hydration) and standard monitoring.\n");
                }
            }
            sb.append("\n");
        }

        // %↑QW
        if (r.pctQwUp() != null) {
            sb.append("### Weekly Load Change (%%↑QW)\n");
            switch (pctL) {
                case "risco" -> {
                    sb.append("- Pull **weekly change back to ±10%**: remove optional extras and add one **easy/recovery day**.\n");
                    sb.append("- Limit sharp spikes (e.g., back-to-back high-intensity days); split workloads.\n");
                }
                case "atenção" -> {
                    sb.append("- Keep increments **≤10%** and watch post-session responses; avoid stacking intense drills.\n");
                    sb.append("- If fatigue signs appear, trim session duration slightly or extend rest intervals.\n");
                }
                case "queda_forte" -> {
                    sb.append("- **Progressively rebuild** after the drop: add 5–10% with quality work and appropriate rest.\n");
                    sb.append("- Keep intensity controlled; prioritize consistency over single hard sessions.\n");
                }
                default /* estável / indisponível */ -> {
                    sb.append("- Preserve **stable weekly change (±10%)** and track how athletes tolerate the plan.\n");
                    sb.append("- Small technical exposures are fine; avoid unnecessary spikes.\n");
                }
            }
            sb.append("\n");
        }

        // Monotony
        if (r.monotony() != null) {
            sb.append("### Monotony\n");
            switch (monoL) {
                case "alto_risco" -> {
                    sb.append("- Reduce **monotony**: vary session focus (volume/intensity), insert a **rest/low day** this week.\n");
                    sb.append("- Diversify drills (e.g., technical vs. conditioning) to avoid repetitive stress.\n");
                }
                case "atenção" -> {
                    sb.append("- Introduce **variation** in intensity zones and drill selection within the week.\n");
                    sb.append("- Avoid identical back-to-back sessions; tweak density and duration.\n");
                }
                default /* saudável / indisponível */ -> {
                    sb.append("- Keep **varied distribution** through the week to maintain healthy monotony.\n");
                    sb.append("- Continue alternating stimulus (neuromuscular, metabolic, technical).\n");
                }
            }
            sb.append("\n");
        }

        // Strain
        if (r.strain() != null) {
            sb.append("### Strain\n");
            switch (strainL) {
                case "alto_risco" -> {
                    sb.append("- Manage **strain**: shorten sets, lower density, extend recovery windows; consider active recovery.\n");
                    sb.append("- De-emphasize high-impact drills for 2–3 days and reassess readiness.\n");
                }
                case "atenção" -> {
                    sb.append("- Slightly reduce session density or total volume; add mobility and recovery practices.\n");
                    sb.append("- Monitor next-day freshness; avoid pairing intense sessions consecutively.\n");
                }
                default /* baixo / indisponível */ -> {
                    sb.append("- Maintain appropriate load; small progressions are acceptable if other metrics are controlled.\n");
                    sb.append("- Keep standard recovery hygiene and monitoring routine.\n");
                }
            }
            sb.append("\n");
        }

        // Combinações críticas (opcional, mantém uma regra combinada no final)
        boolean highACWR = "risco".equals(acwrL);
        boolean highMono = "alto_risco".equals(monoL);
        boolean highStr = "alto_risco".equals(strainL);
        if ((r.acwr() != null || r.monotony() != null || r.strain() != null) && ((highACWR && highMono) || (highACWR && highStr) || (highMono && highStr))) {
            sb.append("- **Combination note**: apply a short **deload** (3–5 days) and monitor **daily** (wellness/RPE); reintroduce intensity only once indices normalize.\n\n");
        }

        sb.append("Please seek professional medical support if symptoms persist or worsen.\n");
        return sb.toString();
    }

    Double d(Integer v) {
        return v == null ? null : v.doubleValue();
    }
}
