package com.simada_backend.dto.response;

public record UserResponseDTO(
        Long id,
        String email,
        String name,
        String userType,
        String userPhoto
) {}