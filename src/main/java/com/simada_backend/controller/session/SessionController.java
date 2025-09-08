package com.simada_backend.controller.session;

import com.simada_backend.dto.request.session.RegisterSessionRequest;
import com.simada_backend.dto.response.coach.CoachSessionDTO;
import com.simada_backend.service.session.SessionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/session")
@CrossOrigin(origins = "http://localhost:3000")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/get")
    public List<CoachSessionDTO> sessions(
            @RequestParam int coachId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate to,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return sessionService.getSessionsCoach(coachId, from, to, limit);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerSession(@Valid @RequestBody RegisterSessionRequest request) {
        sessionService.registerSession(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
