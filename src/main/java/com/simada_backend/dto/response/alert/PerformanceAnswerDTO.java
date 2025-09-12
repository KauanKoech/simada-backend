package com.simada_backend.dto.response.alert;

public record PerformanceAnswerDTO(
        Long id,
        Long athleteId,
        String athleteName,
        String athleteEmail,
        String athletePosition,
        String athletePhoto,
        String athleteNationality,

        String qwStart,
        String createdAt,

        Double acwr,
        Double monotony,
        Double strain,
        Double pctQwUp,

        String acwrLabel,
        String monotonyLabel,
        String strainLabel,
        String pctQwUpLabel
) {
}