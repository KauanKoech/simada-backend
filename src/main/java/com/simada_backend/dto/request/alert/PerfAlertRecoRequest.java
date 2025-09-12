package com.simada_backend.dto.request.alert;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public record PerfAlertRecoRequest(
        @NotNull Integer acwr,
        @NotNull Integer monotony,
        @NotNull Integer strain,
        @NotNull Integer pctQwUp
) {}
