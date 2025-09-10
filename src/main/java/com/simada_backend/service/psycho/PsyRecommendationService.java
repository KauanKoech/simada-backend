package com.simada_backend.service.psycho;

import com.simada_backend.dto.request.psychoForm.PsyRecoRequest;
import com.simada_backend.domain.psycho.PsyRecommendation;
import com.simada_backend.domain.psycho.PsyRecommendation.Source;
import com.simada_backend.integrations.GroqClient;
import com.simada_backend.repository.psycho.PsyRecommendationRepository;
import com.simada_backend.repository.session.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PsyRecommendationService {

    private final GroqClient groqClient;
    private final PsyRecommendationRepository psyRecommendationRepository;
    private final SessionRepository sessionRepository;

    @Value("${groq.model:}")
    private String modelConfigured;

    @Transactional(readOnly = true)
    public Mono<String> generateRecommendations(Long sessionId, Long athleteId, PsyRecoRequest req) {
        return Mono.fromCallable(() ->
                        psyRecommendationRepository.findBySessionIdAndAthleteId(sessionId, athleteId)
                )
                .flatMap(opt -> opt.map(rec -> Mono.just(rec.getText()))
                        .orElseGet(() -> callGroqAndPersist(sessionId, athleteId, req)));
    }

    private Mono<String> callGroqAndPersist(Long sessionId, Long athleteId, PsyRecoRequest req) {
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
                        ? Mono.just(saveAndReturn(sessionId, athleteId, finalCoachId, req, text, Source.groq))
                        : Mono.error(new IllegalStateException("Empty response from Groq")))
                // Erros (timeout, 4xx/5xx, vazio) -> fallback
                .onErrorResume(ex -> {
                    String fb = fallback(req);
                    return Mono.just(saveAndReturn(sessionId, athleteId, finalCoachId, req, fb, Source.fallback));
                });
    }

    private String saveAndReturn(Long sessionId, Long athleteId, Long coachId,
                                 PsyRecoRequest req, String text, Source source) {
        PsyRecommendation rec = PsyRecommendation.builder()
                .sessionId(sessionId)
                .athleteId(athleteId)
                .coachId(coachId)
                .text(text)
                .lang("en")
                .model(source == Source.groq ? modelConfigured : null)
                .source(source)
                .srpe(req.srpe())
                .fatigue(req.fatigue())
                .soreness(req.soreness())
                .mood(req.mood())
                .energy(req.energy())
                .createdAt(Instant.now())
                .build();
        return saveBlocking(rec).getText();
    }

    @Transactional
    protected PsyRecommendation saveBlocking(PsyRecommendation rec) {
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

    // Fallback em inglês (com linha final obrigatória)
    private String fallback(PsyRecoRequest r) {
        int riskCount = 0;
        boolean highFatigue = r.fatigue() >= 7; if (highFatigue) riskCount++;
        boolean highSoreness = r.soreness() >= 7; if (highSoreness) riskCount++;
        boolean lowMood = r.mood() <= 3; if (lowMood) riskCount++;
        boolean lowEnergy = r.energy() <= 3; if (lowEnergy) riskCount++;
        boolean highSRPE = r.srpe() >= 7; if (highSRPE) riskCount++;

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
}
