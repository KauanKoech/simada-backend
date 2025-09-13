package com.simada_backend.ingest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final CsvIngestService ingestService;

    @Operation(summary = "Ingere CSV de métricas para um coach")
    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestCsvResponseDTO> uploadCsv(
            @RequestParam("coachId") Long coachId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "charset", required = false) String charset
    ) throws IOException {

        try (InputStream is = prepareInputStream(file, charset)) {
            IngestCsvResponseDTO dto = ingestService.ingestCsv(coachId, is);
            return ResponseEntity.ok(dto);
        }
    }

    private InputStream prepareInputStream(MultipartFile file, String charset) throws IOException {
        if (charset != null && !charset.isBlank() && !StandardCharsets.UTF_8.name().equalsIgnoreCase(charset)) {
            Charset cs = Charset.forName(charset);
            try (Reader r = new InputStreamReader(file.getInputStream(), cs)) {
                StringWriter sw = new StringWriter();
                r.transferTo(sw);
                return new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
            }
        } else {
            return file.getInputStream();
        }
    }
}