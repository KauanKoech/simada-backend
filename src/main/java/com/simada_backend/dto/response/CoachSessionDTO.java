package com.simada_backend.dto.response;

import java.time.LocalDate;

public record CoachSessionDTO(
        Long id,
        Long coachId,
        String coachPhoto,
        LocalDate date,
        String type,
        String title,
        Integer athleteCount,
        String score,
        String description,
        String location,
        Boolean has_metrics,
        Boolean has_psycho
) {}
