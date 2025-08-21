package com.simada_backend.controller;

import com.simada_backend.dto.request.LoginRequest;
import com.simada_backend.dto.request.RegisterAthleteRequest;
import com.simada_backend.dto.request.RegisterTrainerRequest;
import com.simada_backend.model.Usuario;
import com.simada_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register/trainer")
    public void registerTrainer(@RequestBody RegisterTrainerRequest request) {
        authService.registerTrainer(request);
    }

    @PostMapping("/register/athlete")
    public void registerAthlete(@RequestBody RegisterAthleteRequest request) {
        authService.registerAthlete(request);
    }

    @PostMapping("/login")
    public Usuario login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
