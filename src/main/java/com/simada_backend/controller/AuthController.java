package com.simada_backend.controller;

import com.simada_backend.model.Athlete;
import com.simada_backend.model.Trainer;
import com.simada_backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000") // permite o front consumir
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registerTrainer")
    public String registerTrainer(@RequestBody Trainer trainer) {
        authService.registerTrainer(trainer);
        return "Dados recebidos com sucesso!";
    }

    @PostMapping("/loginTrainer")
    public String loginTrainer(@RequestBody Trainer trainer) {
        authService.loginTrainer(trainer);
        return "Dados recebidos com sucesso!";
    }

    @PostMapping("/registerAthlete")
    public String registerAthlete(@RequestBody Athlete athlete) {
        authService.registerAthlete(athlete);
        return "Dados recebidos com sucesso!";
    }

    @PostMapping("/loginTrainer")
    public String loginAthlete(@RequestBody Athlete athlete) {
        authService.loginAthlete(athlete);
        return "Dados recebidos com sucesso!";
    }
}