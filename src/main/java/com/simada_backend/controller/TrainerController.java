package com.simada_backend.controller;

import com.simada_backend.dto.response.*;
import com.simada_backend.dto.response.athlete.AthleteDTO;
import com.simada_backend.repository.trainer.TrainerRepository;
import com.simada_backend.service.TrainerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
@CrossOrigin(origins = "http://localhost:3000")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainerRepository treinadorRepo;

    public TrainerController(TrainerService trainerService, TrainerRepository treinadorRepo) {
        this.trainerService = trainerService;
        this.treinadorRepo = treinadorRepo;
    }
    @GetMapping("/top-performers")
    public List<TopPerformerDTO> topPerformers(
            @RequestParam(name = "limit", defaultValue = "3") int limit
    ) {

        return trainerService.getTopPerformers(limit);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(
            @RequestParam int trainerId
    ) {
        return trainerService.getTrainerStats(trainerId);
    }

    @GetMapping("/alerts")
    public List<AlertDTO> alerts(
            @RequestParam int trainerId,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String category
    ) {
        return trainerService.getTrainerAlerts(trainerId, days, limit, category);
    }

    @GetMapping("/athletes")
    public List<AthleteDTO> myAthletes(
            @RequestParam Integer trainerId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {

        return trainerService.getAthletesTrainer(trainerId, q, limit, offset);
    }
}