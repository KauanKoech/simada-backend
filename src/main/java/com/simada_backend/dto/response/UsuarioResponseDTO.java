package com.simada_backend.dto.response;

public record UsuarioResponseDTO(
        Long id,
        String email,
        String nome,
        String tipoUsuario,
        String fotoUsuario
) {}