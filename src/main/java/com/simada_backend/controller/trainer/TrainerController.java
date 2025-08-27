package com.simada_backend.controller.trainer;

import com.simada_backend.dto.response.AlertDTO;
import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.service.TrainerService;
import org.springframework.web.bind.annotation.*;

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
    ){
        return service.getTrainerAlerts(trainerId, days, limit, category);
    }
}