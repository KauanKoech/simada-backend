package com.simada_backend.ingest.suport;

import org.apache.commons.csv.CSVRecord;

public final class CsvUtils {
    private CsvUtils(){}

    public static String getByAliases(CSVRecord r, CsvHeaders h) {
        for (String key : h.aliases) {
            if (r.isMapped(key)) {
                String v = r.get(key);
                if (v != null && !v.isBlank()) return v.trim();
            }
        }
        return null;
    }
}