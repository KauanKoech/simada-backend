package com.simada_backend.dto.response.coach;


public record CoachProfileDTO(
        Long id,
        String name,
        String email,
        String gender,
        String team,
        String phone,
        String photoUrl
) {}
