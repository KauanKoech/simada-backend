package com.simada_backend.service.loadCalculator;

import java.util.List;

public record WeeklyLoadResponse(
        String ccMethod,
        List<WeeklyLoadRow> rows
) {
}