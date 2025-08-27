package com.simada_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record AlertDTO(
        Long id,
        LocalDateTime date,
        String type,
        String message,
        String status,
        String action,

        String athleteName,
        String athletePhoto,

        // PERFORMANCE (opcionais)
        Double prevValue,
        Double currValue,
        Double percent,
        String unit,

        // PSICO (opcionais)
        String fatigue,
        String mood,
        Integer hoursSlept
) {}