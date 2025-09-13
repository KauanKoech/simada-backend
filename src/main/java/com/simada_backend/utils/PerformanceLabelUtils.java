package com.simada_backend.utils;

public class PerformanceLabelUtils {
    public static int scoreAcwr(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "ótimo" -> +3;
            case "baixo" -> +1;
            case "atenção" -> -1;
            case "risco" -> -3;
            default -> 0; // "indisponível"
        };
    }

    public static int scorePctQwUp(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "estável" -> +2;
            case "queda_forte" -> +1;
            case "atenção" -> -1;
            case "risco" -> -2;
            default -> 0;
        };
    }

    public static int scoreMonotony(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "saudável" -> +2;
            case "atenção" -> -1;
            case "alto_risco" -> -3;
            default -> 0;
        };
    }

    public static int scoreStrain(String label) {
        if (label == null) return 0;
        return switch (label) {
            case "baixo" -> +1;
            case "atenção" -> -1;
            case "alto_risco" -> -2;
            default -> 0;
        };
    }
}
