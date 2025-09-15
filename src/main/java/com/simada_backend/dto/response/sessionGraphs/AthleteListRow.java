package com.simada_backend.dto.response.sessionGraphs;

public record AthleteListRow(
        Long id,
        String name,
        String position,
        Integer jerseyNumber,
        String avatarUrl
) {
}
