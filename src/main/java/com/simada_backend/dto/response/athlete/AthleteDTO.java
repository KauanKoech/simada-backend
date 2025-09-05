package com.simada_backend.dto.response.athlete;

public record AthleteDTO(
        Long id,
        String name,
        String email,
        String birth,
        String phone,
        String shirt_number,
        String position,
        String avatarUrl,
        AthleteExtraDTO extra
) {}