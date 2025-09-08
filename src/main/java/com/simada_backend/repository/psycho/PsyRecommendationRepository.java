package com.simada_backend.repository.psycho;

import com.simada_backend.domain.psycho.PsyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsyRecommendationRepository extends JpaRepository<PsyRecommendation, Long> {

    Optional<PsyRecommendation> findBySessionIdAndAthleteId(Long sessionId, Long athleteId);
}
