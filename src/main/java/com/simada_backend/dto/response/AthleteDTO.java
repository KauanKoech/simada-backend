package com.simada_backend.dto.response;

import java.time.LocalDate;

public record AthleteDTO(
        Long id,
        String name,
        String email,
        LocalDate birth,
        String phone,
        String avatarUrl,
        String status
) {}