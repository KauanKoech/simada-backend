package com.simada_backend.service.alert;

import com.simada_backend.dto.request.alert.PerfAlertRecoRequest;
import com.simada_backend.dto.request.alert.PsyAlertRecoRequest;
import com.simada_backend.model.recommendation.PerfRecommendation;
import com.simada_backend.model.recommendation.PsyRecommendation;
import com.simada_backend.integrations.GroqClient;
import com.simada_backend.repository.recommendation.PerfRecommendationRepository;
import com.simada_backend.repository.recommendation.PsyRecommendationRepository;
import com.simada_backend.repository.session.SessionRepository;
import com.simada_backend.service.loadCalculator.Labels;
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
                focused on TRAINING LOAD MANAGEMENT using four indices:
                - ACWR (acute:chronic workload ratio)
                - Weekly Load Change (%%↑QW vs previous week)
                - Monotony (Foster)
                - Strain (Foster)
                
                Rules:
                - Output format: markdown with 3–7 concise bullets (each 1–2 sentences).
                - Language: English, pragmatic and empathetic tone.
                - Do not make medical diagnoses; focus on training load adjustments, recovery hygiene, and monitoring.
                - Always tailor advice to the combination of indices (not just one metric in isolation).
                - Prefer specific actions coaches/athletes can take next session and next week.
                - IMPORTANT: End with this final line:
                  "⚠️ Please seek professional medical support if symptoms persist or worsen."
                
                Guidelines to interpret indices (do not restate them verbatim; just use them to guide advice):
                - ACWR:
                  <0.8 = low; 0.8–1.3 = optimal; 1.3–1.5 = caution; >1.5 = risk.
                - Weekly Load Change (%%↑QW):
                  < -10%% = large drop; -10%%–10%% = stable; 10%%–20%% = caution; >20%% = risk.
                - Monotony:
                  <1.0 = healthy; 1.0–2.0 = caution; >2.0 = high risk.
                - Strain:
                  <6000 = low; 6000–8000 = caution; >8000 = high risk.
                """;


        String perfUserPrompt = """
                Session/Athlete context:
                - sessionId: %d
                - athleteId: %d
                
                Training load indices:
                - ACWR: %s (%s)
                - Weekly Load Change (%%↑QW): %s%% (%s)
                - Monotony: %s (%s)
                - Strain: %s (%s)
                
                Expected output:
                - Markdown list of 3–7 practical bullets (1–2 sentences each) with next-session and next-week actions.
                - Consider combinations (e.g., high ACWR + high monotony → stronger deload and distribution).
                - The last line must always be:
                  "⚠️ Please seek professional medical support if symptoms persist or worsen."
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
        sb.append("**Recommendations (fallback):**\n");

        if ("risco".equals(acwrL) || "atenção".equals(acwrL)) {
            sb.append("- Adjust **next-week load**: reduce overall volume by 10–20% and cap high-intensity exposures; prefer distributed sessions over single peaks.\n");
        } else if ("baixo".equals(acwrL)) {
            sb.append("- Gradually **reintroduce load** (+5–10% next week) to move toward the optimal ACWR zone while monitoring responses.\n");
        }
        if ("risco".equals(pctL) || "atenção".equals(pctL)) {
            sb.append("- Keep weekly change within **±10%**: trim extras (conditioning blocks, small-sided games) and add one **easy/recovery day**.\n");
        } else if ("queda_forte".equals(pctL)) {
            sb.append("- After a large drop, **progressively rebuild** with controlled increments (<10%) and maintain technical quality.\n");
        }
        if ("alto_risco".equals(monoL) || "atenção".equals(monoL)) {
            sb.append("- Reduce **monotony**: vary session focus (volume/intensity), insert a **rest/low day**, and diversify drills across the week.\n");
        }

        if ("alto_risco".equals(strainL) || "atenção".equals(strainL)) {
            sb.append("- Manage **strain**: shorten sets, lower density, and extend **recovery windows** (sleep, hydration, mobility, cold/warm strategies as tolerated).\n");
        }
        boolean highACWR = "risco".equals(acwrL);
        boolean highMono = "alto_risco".equals(monoL);
        boolean highStr = "alto_risco".equals(strainL);
        if ((highACWR && highMono) || (highACWR && highStr) || (highMono && highStr)) {
            sb.append("- Apply a short **deload** (3–5 days) and monitor **daily** (wellness/RPE); only reintroduce intensity once symptoms and indices normalize.\n");
        }
        sb.append("\n⚠️ Please seek professional medical support if symptoms persist or worsen.\n");
        return sb.toString();
    }

    Double d(Integer v) {
        return v == null ? null : v.doubleValue();
    }
}
