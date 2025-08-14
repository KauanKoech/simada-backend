package com.simada_backend.controller;

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

    @PostMapping("/register")
    public String register(@RequestBody Trainer trainer) {
        authService.registerTrainer(trainer);
        return "Dados recebidos com sucesso!";
    }
}