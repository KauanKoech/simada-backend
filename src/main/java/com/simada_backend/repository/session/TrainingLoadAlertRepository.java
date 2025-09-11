package com.simada_backend.repository.session;

import com.simada_backend.model.session.TrainingLoadAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingLoadAlertRepository extends JpaRepository<TrainingLoadAlert, Long> {
    Optional<TrainingLoadAlert> findByAthleteIdAndSessionId(Long athleteId, Long sessionId);
}