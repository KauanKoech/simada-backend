package com.simada_backend.model.loadCalc;

import java.util.List;

public record WeeklyLoadResponse(
        String ccMethod,
        List<WeeklyLoadRow> rows
) {
}