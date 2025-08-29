package com.simada_backend.controller;

import com.simada_backend.dto.request.RegisterSessionRequest;
import com.simada_backend.dto.response.SessionDTO;
import com.simada_backend.dto.response.TrainerSessionDTO;
import com.simada_backend.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public List<TrainerSessionDTO> sessions(
            @RequestParam int trainerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return sessionService.getSessionsTrainer(trainerId, from, to, limit);
    }

    @PostMapping("/register")
    public SessionDTO registerTrainer(@Valid @RequestBody RegisterSessionRequest request) {
        return sessionService.registerSession(request);
    }
}
