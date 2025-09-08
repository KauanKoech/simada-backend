package com.simada_backend.dto.response.psycho;

import java.time.LocalDateTime;

public record PsychoAnswerAthleteDTO(
        Long answerId,
        Long athleteId,
        String athleteName,
        String athleteEmail,
        String athletePosition,
        String athletePhoto,
        LocalDateTime submittedAt,
        Integer srpe,
        Integer fatigue,
        Integer soreness,
        Integer mood,
        Integer energy,
        String token
){}
