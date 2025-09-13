package com.simada_backend.ingest;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngestCsvResponseDTO {
    private Long fileId;
    private String filename;

    private int totalRows;
    private int okRows;
    private int errorRows;

    @Singular("error")
    private List<String> sampleErrors;
}