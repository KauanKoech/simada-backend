package com.simada_backend.controller;

import com.simada_backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000") // ou a porta do seu front
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String registrarTreinador(@RequestBody TreinadorRequest request) {
        try {
            return authService.registrarTreinador(
                    request.getNome(),
                    request.getEmail(),
                    request.getSenha()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar treinador: " + e.getMessage());
        }
    }
}