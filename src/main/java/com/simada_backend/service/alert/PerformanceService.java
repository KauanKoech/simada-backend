package com.simada_backend.service.alert;

import com.simada_backend.api.error.BusinessException;
import com.simada_backend.api.error.ErrorCode;
import com.simada_backend.dto.response.alert.PerformanceAlertDTO;
import com.simada_backend.dto.response.alert.PerformanceAnswerDTO;
import com.simada_backend.model.session.TrainingLoadAlert;
import com.simada_backend.repository.alert.TrainingLoadAlertRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
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

    public Optional<PerformanceAnswerDTO> getBySessionAndAthlete(Long sessionId, Long athleteId) {
        System.out.println("➡️ getBySessionAndAthlete chamado com sessionId = " + sessionId +
                ", athleteId = " + athleteId);

        Optional<PerformanceAnswerDTO> result = repo.findAnswerBySessionAndAthlete(sessionId, athleteId);

        if (result.isPresent()) {
            System.out.println("✅ Encontrado PerformanceAnswerDTO: " + result.get());
        } else {
            System.out.println("⚠️ Nenhum PerformanceAnswerDTO encontrado para sessionId = " + sessionId +
                    ", athleteId = " + athleteId);
        }
        return result;
    }


    @Transactional
    public void deleteAlert(Long alertId, Long coachId) {
        TrainingLoadAlert alert = repo.findByIdAndCoach_Id(alertId, coachId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Training load alert not found for this coach."
                ));
        repo.delete(alert);
    }
}
