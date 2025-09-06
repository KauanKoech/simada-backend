package com.simada_backend.controller;

import com.simada_backend.dto.response.*;
import com.simada_backend.dto.response.athlete.AthleteDTO;
import com.simada_backend.repository.coach.CoachRepository;
import com.simada_backend.service.CoachService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "http://localhost:3000")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }
    @GetMapping("/top-performers")
    public List<TopPerformerDTO> topPerformers(
            @RequestParam(name = "limit", defaultValue = "3") int limit
    ) {

        return coachService.getTopPerformers(limit);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(
            @RequestParam int coachId
    ) {
        return coachService.getCoachStats(coachId);
    }

    @GetMapping("/alerts")
    public List<AlertDTO> alerts(
            @RequestParam int coachId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category
    ) {
        return coachService.getCoachAlerts(coachId, days, limit, category);
    }

    @GetMapping("/athletes")
    public List<AthleteDTO> myAthletes(
            @RequestParam Integer coachId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {

        return coachService.getAthletesCoach(coachId, q, limit, offset);
    }
}