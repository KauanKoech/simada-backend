package com.simada_backend.controller.session;

import com.simada_backend.dto.request.session.UpdateNotesRequest;
import com.simada_backend.dto.request.session.UpdateSessionRequest;
import com.simada_backend.dto.response.SessionDTO;
import com.simada_backend.service.loadCalc.CsvParsingException;
import com.simada_backend.service.session.SessionMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/coach/sessions")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class SessionMetricsController {

    private final SessionMetricsService service;

    @PostMapping("/{sessionId}/metrics/import")
    public ResponseEntity<Void> importMetrics(
            @PathVariable("sessionId") int sessionId,
            @RequestParam("file") MultipartFile file
    ) throws CsvParsingException {
        service.importMetricsFromCsv(sessionId, file);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update/notes/{id}")
    public ResponseEntity<Void> updateNotes(
            @PathVariable int id,
            @RequestBody UpdateNotesRequest body
    ) {
        service.updateSessionNotes(id, body.description());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<SessionDTO> update(
            @PathVariable int id,
            @RequestBody UpdateSessionRequest body
    ) {
        SessionDTO updated = service.updateSession(id, body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{sessionId}/metrics")
    public ResponseEntity<Void> deleteSessionMetrics(@PathVariable Long sessionId,
                                                     @RequestParam Long coachId) {
        service.deleteSessionMetrics(sessionId, coachId);
        return ResponseEntity.noContent().build();
    }
}