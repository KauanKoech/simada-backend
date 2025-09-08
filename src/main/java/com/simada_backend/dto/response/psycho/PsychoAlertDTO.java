package com.simada_backend.dto.response.psycho;


import java.time.LocalDate;

public record PsychoAlertDTO(
        Long alertId,
        Long athleteId,
        Long sessionId,
        Long answerId,
        String athleteName,
        String athletePhoto,
        Integer srpe,
        Integer fatigue,
        Integer soreness,
        Integer mood,
        Integer energy,
        Integer total,
        String risk,
        LocalDate date
) {}