package com.simada_backend.dto.response;

import java.time.LocalDateTime;

public record TrainerSessionDTO(
        Long id,
        Long trainerId,
        String trainerPhoto,
        LocalDateTime start,
        LocalDateTime end,
        String type,
        String title,
        Integer athleteCount,
        String score,
        String description,
        String location
) {}
