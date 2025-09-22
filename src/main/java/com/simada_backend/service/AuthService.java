package com.simada_backend.service;

import com.simada_backend.api.error.BusinessException;
import com.simada_backend.api.error.ErrorCode;
import com.simada_backend.dto.request.LoginRequest;
import com.simada_backend.dto.request.athlete.RegisterAthleteRequest;
import com.simada_backend.dto.request.RegisterCoachRequest;
import com.simada_backend.dto.response.UserResponseDTO;
import com.simada_backend.model.User;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.Coach;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.coach.CoachRepository;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CoachRepository coachRepository;
    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    @Transactional
    public UserResponseDTO registerCoach(RegisterCoachRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_IN_USE,
                    HttpStatus.CONFLICT,
                    "This email is already registered."
            );
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoto("");
        user.setUserType("coach");
        userRepository.save(user);

        Coach coach = new Coach();
        coach.setName(request.getName());
        coach.setId(user.getId());
        coach.setUser(user);
        coachRepository.save(coach);

        String token = jwt.generate(user);

        return new UserResponseDTO(user.getId(), user.getEmail(), user.getName(),
                user.getUserType(), user.getPhoto(), token);
    }

    @Transactional
    public UserResponseDTO registerAthlete(RegisterAthleteRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_IN_USE,
                    HttpStatus.CONFLICT,
                    "This email is already registered."
            );
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoto("");
        user.setUserType("athlete");
        userRepository.save(user);

        Coach coach = coachRepository.findById((long) request.getIdCoach())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Coach not found."
                ));

        Athlete athlete = new Athlete();
        athlete.setName(request.getName());
        athlete.setId(user.getId());
        athlete.setJerseyNumber(request.getJerseyNumber());
        athlete.setPosition(request.getPosition());
        athlete.setUser(user);
        athlete.setCoach(coach);
        athleteRepository.save(athlete);

        String token = jwt.generate(user);

        return new UserResponseDTO(user.getId(), user.getEmail(), user.getName(),
                user.getUserType(), user.getPhoto(), token);
    }


    public UserResponseDTO login(LoginRequest request) {
        User user = userRepository
                .findFirstByEmailOrderByIdDesc(request.getEmail())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AUTH_INVALID_CREDENTIALS,
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials"
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(
                    ErrorCode.AUTH_INVALID_CREDENTIALS,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        String token = jwt.generate(user);

        return new UserResponseDTO(user.getId(), user.getEmail(), user.getName(),
                user.getUserType(), user.getPhoto(), token);
    }
}
