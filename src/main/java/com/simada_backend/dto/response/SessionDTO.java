package com.simada_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record SessionDTO(
        Long id,
        @JsonProperty("coach_id") Long coachId,
        @JsonProperty("coach_photo") String coachPhoto,
        LocalDate date,
        String type,
        String title,
        @JsonProperty("athletes_count") Integer athletesCount,
        String score,
        String description,
        String location
) {}
