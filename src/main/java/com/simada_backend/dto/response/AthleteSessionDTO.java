package com.simada_backend.dto.response;

import java.time.LocalDate;

public record AthleteSessionDTO(
        Long id,
        String coachPhoto,
        LocalDate date,
        String type,
        String title,
        Integer athleteCount,
        String score,
        String description,
        String location,
        Boolean has_metrics
) {}
