package com.simada_backend.dto.response.alert;

public record PerformanceAlertDTO(
        Long id,
        Long athleteId,
        Long sessionId,
        Long coachId,

        Double acwr,
        Double monotony,
        Double strain,
        Double pctQwUp,

        String acwrLabel,
        String monotonyLabel,
        String strainLabel,
        String pctQwUpLabel,

        String createdAt,
        String qwStart,

        String athleteName,
        String athletePhoto
) {
}
