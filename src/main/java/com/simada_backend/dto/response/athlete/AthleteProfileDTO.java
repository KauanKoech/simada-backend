package com.simada_backend.dto.response.athlete;

public record AthleteProfileDTO(
        Long id,
        String name,
        String email,
        String gender,
        String phone,
        String nationality,
        String photoUrl
) {
}