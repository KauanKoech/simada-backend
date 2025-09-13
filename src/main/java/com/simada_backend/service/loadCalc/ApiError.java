package com.simada_backend.service.loadCalc;

public record ApiError(String error, String metric, String detail, Integer needAtLeast, Integer have) {}