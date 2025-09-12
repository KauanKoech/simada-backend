package com.simada_backend.service.alert;

import com.simada_backend.dto.response.alert.PerformanceAlertDTO;
import com.simada_backend.dto.response.alert.PerformanceAnswerDTO;
import com.simada_backend.repository.alert.TrainingLoadAlertRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerformanceService {
    private final TrainingLoadAlertRepository repo;

    public PerformanceService(TrainingLoadAlertRepository repo) {
        this.repo = repo;
    }

    public List<PerformanceAlertDTO> getAlertsByCoach(Long coachId) {
        return repo.findByCoachId(coachId);
    }

    public Optional<PerformanceAnswerDTO> getBySessionAndAthlete(Long athleteId) {
        return repo.findAnswerBySessionAndAthlete(athleteId);
    }
}
