package com.simada_backend.controller.session;

import com.simada_backend.repository.session.SessionMetricsQueryRepository;
import com.simada_backend.service.session.SessionChartsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionChartsController {

    private final SessionChartsService service;

    @GetMapping(value = "/{sessionId}/athletes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SessionMetricsQueryRepository.AthleteListRow> listAthletes(@PathVariable Long sessionId) {
        return service.listAthletes(sessionId);
    }

    // GET /api/sessions/{sessionId}/metrics?scope=team
    // GET /api/sessions/{sessionId}/metrics?athleteId=123
    @GetMapping(value = "/{sessionId}/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SessionMetricsQueryRepository.MetricsRowView> metrics(
            @PathVariable Long sessionId,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Long athleteId
    ) {
        if ("team".equalsIgnoreCase(scope)) {
            return service.listMetricsForTeam(sessionId);
        }
        if (athleteId != null) {
            return service.listMetricsForAthlete(sessionId, athleteId);
        }
        return service.listMetricsForTeam(sessionId);
    }
}