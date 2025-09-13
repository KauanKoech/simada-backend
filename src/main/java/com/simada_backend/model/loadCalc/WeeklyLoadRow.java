package com.simada_backend.model.loadCalc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WeeklyLoadRow(
        Long athleteId,
        LocalDate qwStart,
        BigDecimal ca,
        BigDecimal cc,
        BigDecimal acwr,
        String acwrLabel,
        BigDecimal pctQwUp,
        String pctQwUpLabel,
        BigDecimal monotony,
        String monotonyLabel,
        BigDecimal strain,
        String strainLabel,
        Integer daysWithLoad,
        List<String> warnings
) {
}