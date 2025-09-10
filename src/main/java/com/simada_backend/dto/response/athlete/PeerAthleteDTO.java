package com.simada_backend.dto.response.athlete;

public record PeerAthleteDTO(
        Long id,
        String name,
        String email,
        String position,
        String jersey,
        String nationality,
        String avatar,
        Integer points
) {}