package com.simada_backend.repository.loadCalc;

import com.simada_backend.model.loadCalc.SessionLoad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionLoadRepo extends JpaRepository<SessionLoad, Long> {
    Optional<SessionLoad> findBySessionId(Long sessionId);
    Optional<SessionLoad> findBySessionIdAndAthleteId(Long sessionId, Long athleteId);

}
