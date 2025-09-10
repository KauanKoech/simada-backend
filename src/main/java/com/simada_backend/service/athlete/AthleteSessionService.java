package com.simada_backend.service.athlete;

import com.simada_backend.dto.response.AthleteSessionDTO;
import com.simada_backend.dto.response.coach.CoachSessionDTO;
import com.simada_backend.repository.session.AthleteSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class AthleteSessionService {
    @Autowired
    private AthleteSessionRepository athleteSessionRepo;

    public AthleteSessionService(AthleteSessionRepository athleteSessionRepo) {
        this.athleteSessionRepo = Objects.requireNonNull(athleteSessionRepo);
    }

    public List<AthleteSessionDTO> getSessions(Long athleteId) {
        return athleteSessionRepo.findAthleteSessions(athleteId)
                .stream()
                .map(r -> new AthleteSessionDTO(
                        r.getId(),
                        r.getCoachPhoto(),
                        r.getDate(),
                        r.getType(),
                        r.getTitle(),
                        r.getAthleteCount(),
                        r.getScore(),
                        r.getDescription(),
                        r.getLocation(),
                        r.getHas_metrics() != null && r.getHas_metrics() == 1
                ))
                .toList();


    }
}
