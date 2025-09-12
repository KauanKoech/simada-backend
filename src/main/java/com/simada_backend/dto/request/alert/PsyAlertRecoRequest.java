package com.simada_backend.dto.request.alert;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record PsyAlertRecoRequest(
        @NotNull @Min(0) @Max(10) Integer srpe,
        @NotNull @Min(0) @Max(10) Integer fatigue,
        @NotNull @Min(0) @Max(10) Integer soreness,
        @NotNull @Min(0) @Max(10) Integer mood,
        @NotNull @Min(0) @Max(10) Integer energy
) {}
