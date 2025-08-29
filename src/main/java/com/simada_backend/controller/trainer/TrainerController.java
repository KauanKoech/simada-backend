package com.simada_backend.controller.trainer;

import com.simada_backend.dto.response.AlertDTO;
import com.simada_backend.dto.response.AthleteDTO;
import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.dto.response.TrainerSessionDTO;
import com.simada_backend.service.TrainerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
@CrossOrigin(origins = "http://localhost:3000")
public class TrainerController {

    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @GetMapping("/top-performers")
    public List<TopPerformerDTO> topPerformers(
            @RequestParam(name = "limit", defaultValue = "3") int limit
    ) {

        return service.getTopPerformers(limit);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(
            @RequestParam int trainerId
    ) {
        return service.getTrainerStats(trainerId);
    }

    @GetMapping("/alerts")
    public List<AlertDTO> alerts(
            @RequestParam int trainerId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category
    ) {
        return service.getTrainerAlerts(trainerId, days, limit, category);
    }

    @GetMapping("/sessions")
    public List<TrainerSessionDTO> sessions(
            @RequestParam int trainerId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return service.getSessionsTrainer(trainerId, from, to, limit);
    }

    @GetMapping("/athletes")
    public List<AthleteDTO> myAthletes(
            @RequestParam int trainerId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return service.getAthletesTrainer(trainerId, q, status, limit, offset);
    }
}