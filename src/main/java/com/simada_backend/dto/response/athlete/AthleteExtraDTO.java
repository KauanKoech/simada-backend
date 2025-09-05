package com.simada_backend.dto.response.athlete;

import java.math.BigDecimal;

public record AthleteExtraDTO(
        BigDecimal height_cm,
        BigDecimal weight_kg,
        BigDecimal lean_mass_kg,
        BigDecimal fat_mass_kg,
        BigDecimal body_fat_pct,
        String dominant_foot,
        String nationality,
        String injury_status
) {}