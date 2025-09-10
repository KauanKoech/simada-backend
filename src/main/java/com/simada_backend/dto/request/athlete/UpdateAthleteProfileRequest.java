package com.simada_backend.dto.request.athlete;

public record UpdateAthleteProfileRequest(
        String name,
        String email,
        String gender,
        String phone,
        String nationality
) {
}