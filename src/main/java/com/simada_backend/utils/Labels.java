package com.simada_backend.utils;

public final class Labels {
    private Labels() {
    }

    // ACWR – Gabbett
    public static String acwrLabel(Double v) {
        if (v == null) return "unavailable";
        if (v < 0.8) return "low";
        if (v <= 1.3) return "optimal";
        if (v <= 1.5) return "attention";
        return "risk";
    }

    // %↑ QW – Gabbett
    public static String pctQwUpLabel(Double v) {
        if (v == null) return "unavailable";
        if (v < -10) return "sharp_drop";
        if (v <= 10) return "stable";
        if (v <= 20) return "attention";
        return "risk";
    }

    // Monotonia – Foster
    public static String monotonyLabel(Double v) {
        if (v == null) return "unavailable";
        if (v < 1.0) return "healthy";
        if (v <= 2.0) return "attention";
        return "high_risk";
    }

    // Strain – Foster
    public static String strainLabel(Double v) {
        if (v == null) return "unavailable";
        if (v < 6000) return "low";
        if (v <= 8000) return "attention";
        return "high_risk";
    }
}