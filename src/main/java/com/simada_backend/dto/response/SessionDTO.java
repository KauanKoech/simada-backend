package com.simada_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record SessionDTO(
        Long id,
        @JsonProperty("trainer_id") Long trainerId,
        @JsonProperty("trainer_photo") String trainerPhoto,
        LocalDate date,
        String type,
        String title,
        @JsonProperty("athletes_count") Integer athletesCount,
        String score,
        String description,
        String location
) {}
