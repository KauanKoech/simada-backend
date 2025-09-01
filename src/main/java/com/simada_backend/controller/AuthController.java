package com.simada_backend.controller;

import com.simada_backend.dto.request.LoginRequest;
import com.simada_backend.dto.request.athlete.RegisterAthleteRequest;
import com.simada_backend.dto.request.RegisterTrainerRequest;
import com.simada_backend.dto.response.UsuarioResponseDTO;
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
    public UsuarioResponseDTO registerTrainer(@RequestBody RegisterTrainerRequest request) {
        return authService.registerTrainer(request);
    }

    @PostMapping("/register/athlete")
    public UsuarioResponseDTO registerAthlete(@RequestBody RegisterAthleteRequest request) {
        return authService.registerAthlete(request);
    }

    @PostMapping("/login")
    public UsuarioResponseDTO login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
