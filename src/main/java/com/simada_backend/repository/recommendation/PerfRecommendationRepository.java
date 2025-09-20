package com.simada_backend.repository.recommendation;

import com.simada_backend.model.recommendation.PerfRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfRecommendationRepository extends JpaRepository<PerfRecommendation, Long> {
    Optional<PerfRecommendation> findBySession_IdAndAthlete_Id(Long sessionId, Long athleteId);
}

