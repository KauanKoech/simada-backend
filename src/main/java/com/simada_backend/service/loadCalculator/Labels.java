package com.simada_backend.service.loadCalculator;

public final class Labels {
    private Labels() {
    }

    // ACWR – Gabbett
    public static String acwrLabel(Double v) {
        if (v == null) return "indisponível";
        if (v < 0.8) return "baixo";
        if (v <= 1.3) return "ótimo";
        if (v <= 1.5) return "atenção";
        return "risco";
    }

    // %↑ QW – Gabbett
    public static String pctQwUpLabel(Double v) {
        if (v == null) return "indisponível";
        if (v < -10) return "queda_forte";
        if (v <= 10) return "estável";
        if (v <= 20) return "atenção";
        return "risco";
    }

    // Monotonia – Foster
    public static String monotonyLabel(Double v) {
        if (v == null) return "indisponível";
        if (v < 1.0) return "saudável";
        if (v <= 2.0) return "atenção";
        return "alto_risco";
    }

    // Strain – Foster
    public static String strainLabel(Double v) {
        if (v == null) return "indisponível";
        if (v < 6000) return "baixo";
        if (v <= 8000) return "atenção";
        return "alto_risco";
    }
}