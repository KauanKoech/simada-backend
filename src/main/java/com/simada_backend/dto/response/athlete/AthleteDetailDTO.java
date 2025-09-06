package com.simada_backend.dto.response.athlete;

public record AthleteDetailDTO(
    Long id,
    String name,
    String email,
    String phone,
    String birth,
    String avatarUrl,
    String jersey_number,
    String position,
    AthleteExtraDTO extra
) {}