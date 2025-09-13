package com.simada_backend.ingest.suport;

import com.simada_backend.service.loadCalc.CsvParsingException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class ParseUtils {
    private ParseUtils(){}

    // Ordem solicitada: m/d/yyyy, mm/dd/yyyy, m/dd/yyyy, mm/d/yyyy
    private static final DateTimeFormatter[] DATE_PATTERNS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("M/d/uuuu"),
            DateTimeFormatter.ofPattern("MM/dd/uuuu"),
            DateTimeFormatter.ofPattern("M/dd/uuuu"),
            DateTimeFormatter.ofPattern("MM/d/uuuu"),
    };

    public static LocalDate parseDateUSOrder(String raw) throws CsvParsingException {
        String s = normalize(raw);
        for (DateTimeFormatter f : DATE_PATTERNS) {
            try { return LocalDate.parse(s, f); } catch (DateTimeParseException ignore) {}
        }
        throw new CsvParsingException("data inválida: " + raw);
    }

    public static Double parseDouble(String raw) throws CsvParsingException {
        if (raw == null) return null;
        String s = normalize(raw);
        if (s.isBlank()) return null;
        // Tenta padrão
        try { return Double.valueOf(s); } catch (NumberFormatException ignore) {}
        // Tenta vírgula decimal → ponto
        if (s.contains(",")) {
            String s2 = s.replace(".", ""); // remove separador de milhar (se houver)
            s2 = s2.replace(",", ".");
            try { return Double.valueOf(s2); } catch (NumberFormatException ignore) {}
        }
        throw new CsvParsingException("número inválido: " + raw);
    }

    public static Integer parseInteger(String raw) throws CsvParsingException {
        Double d = parseDouble(raw);
        if (d == null) return null;
        return (int) Math.round(d);
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim();
    }
}