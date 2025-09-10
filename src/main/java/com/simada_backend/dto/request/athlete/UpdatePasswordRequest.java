package com.simada_backend.dto.request.athlete;

public record UpdatePasswordRequest(
        String currentPassword,
        String newPassword
) {
}