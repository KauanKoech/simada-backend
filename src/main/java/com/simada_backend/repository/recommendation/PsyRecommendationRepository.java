package com.simada_backend.repository.recommendation;

import com.simada_backend.model.recommendation.PsyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsyRecommendationRepository extends JpaRepository<PsyRecommendation, Long> {
    Optional<PsyRecommendation> findBySession_IdAndAthlete_Id(Long sessionId, Long athleteId);
}
