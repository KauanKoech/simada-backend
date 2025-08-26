package com.simada_backend.controller.trainer;

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
    public Map<String, Object> stats(@RequestParam int trainerId) {
        return service.getTrainerStats(trainerId);
    }
}