package com.simada_backend.utils;

public class PerformanceLabelUtils {
    public static int scoreAcwr(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "optimal" -> +3;
            case "low" -> +1;
            case "attention" -> -1;
            case "risk" -> -3;
            default -> 0; // "indisponível"
        };
    }

    public static int scorePctQwUp(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "stable" -> +2;
            case "sharp_drop" -> +1;
            case "attention" -> -1;
            case "risk" -> -2;
            default -> 0;
        };
    }

    public static int scoreMonotony(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "healthy" -> +2;
            case "attention" -> -1;
            case "high_risk" -> -3;
            default -> 0;
        };
    }

    public static int scoreStrain(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "low" -> +1;
            case "attention" -> -1;
            case "high_risk" -> -2;
            default -> 0;
        };
    }
}
