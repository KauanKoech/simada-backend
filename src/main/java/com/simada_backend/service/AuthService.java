package com.simada_backend.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CoachRepository coachRepository;
    private final AthleteRepository athleteRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       CoachRepository coachRepository,
                       AthleteRepository athleteRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.coachRepository = Objects.requireNonNull(coachRepository);
        this.athleteRepository = Objects.requireNonNull(athleteRepository);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Transactional
    public UserResponseDTO registerCoach(RegisterCoachRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoto("");
        user.setUserType("treinador");
        userRepository.save(user);

        Coach coach = new Coach();
        coach.setName(request.getFullName());
        coach.setUser(user);
        coachRepository.save(coach);

        return new UserResponseDTO(user.getId(), user.getEmail(), user.getName(),
                user.getUserType(), user.getPhoto());
    }

    @Transactional
    public UserResponseDTO registerAthlete(RegisterAthleteRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoto("");
        user.setUserType("atleta");
        userRepository.save(user);

        Coach coach = coachRepository.findById((long) request.getIdCoach())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Treinador não encontrado"));

        Athlete athlete = new Athlete();
        athlete.setName(request.getName());
        athlete.setJerseyNumber(request.getShirtNumber());
        athlete.setPosition(request.getPosition());
        athlete.setUser(user);
        athlete.setCoach(coach);
        athleteRepository.save(athlete);

        return new UserResponseDTO(user.getId(), user.getEmail(), user.getName(),
                user.getUserType(), user.getPhoto());
    }


    public UserResponseDTO login(LoginRequest request) {
        User user = userRepository
                .findFirstByEmailOrderByIdDesc(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        return new UserResponseDTO(user.getId(), user.getEmail(), user.getName(),
                user.getUserType(), user.getPhoto());
    }
}
