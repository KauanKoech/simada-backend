package com.simada_backend.controller;

import com.simada_backend.dto.request.LoginRequest;
import com.simada_backend.dto.request.athlete.RegisterAthleteRequest;
import com.simada_backend.dto.request.RegisterCoachRequest;
import com.simada_backend.dto.response.UserResponseDTO;
import com.simada_backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/coach")
    public UserResponseDTO registerCoach(@RequestBody RegisterCoachRequest request) {
        return authService.registerCoach(request);
    }

    @PostMapping("/register/athlete")
    public UserResponseDTO registerAthlete(@RequestBody RegisterAthleteRequest request) {
        return authService.registerAthlete(request);
    }

    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
