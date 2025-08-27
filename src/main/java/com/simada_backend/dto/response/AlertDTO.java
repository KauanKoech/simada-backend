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

        Double prevValue,
        Double currValue,
        Double percent,
        String unit
) {}