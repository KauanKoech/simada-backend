package com.simada_backend.controller.coach;

import com.simada_backend.dto.response.*;
import com.simada_backend.dto.response.athlete.AthleteDTO;
import com.simada_backend.service.coach.CoachService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coach")
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

    @GetMapping("/athletes")
    public List<AthleteDTO> myAthletes(
            @RequestParam Integer coachId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {

        return coachService.getAthletesCoach(coachId, q, limit, offset);
    }
}