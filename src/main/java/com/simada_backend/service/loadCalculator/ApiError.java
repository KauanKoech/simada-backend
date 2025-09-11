package com.simada_backend.service.loadCalculator;

public record ApiError(String error, String metric, String detail, Integer needAtLeast, Integer have) {}