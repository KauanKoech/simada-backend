package com.simada_backend.controller;

import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.service.TrainerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trainer")
public class TrainerController {

    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @GetMapping("/top-performers")
    public List<TopPerformerDTO> topPerformers(
            @RequestParam(name = "limit", defaultValue = "3") int limit
    ) {

        return service.getTopPerformers(limit /*, null */);
    }
}