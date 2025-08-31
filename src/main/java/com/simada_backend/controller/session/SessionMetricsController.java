package com.simada_backend.controller.session;

import com.simada_backend.service.session.SessionMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/trainer/sessions")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class SessionMetricsController {

    private final SessionMetricsService service;

    @PostMapping("/{sessionId}/metrics/import")
    public ResponseEntity<Void> importMetrics(
            @PathVariable("sessionId") int sessionId,
            @RequestParam("file") MultipartFile file
    ) {
        service.importMetricsFromCsv(sessionId, file);
        return ResponseEntity.ok().build();
    }
}