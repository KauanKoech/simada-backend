package com.simada_backend.ingest;

import java.io.InputStream;

public interface CsvIngestService {
    /**
     * Ingere um CSV de métricas vinculando atletas e sessões a um coach.
     *
     * @param coachId   id do treinador (existente no banco)
     * @param csvStream stream do arquivo CSV (ex.: file.getInputStream())
     * @return resumo do processamento
     */
    IngestCsvResponseDTO ingestCsv(Long coachId, InputStream csvStream);
}