package com.simada_backend.repository.recommendation;

import com.simada_backend.model.recommendation.PsyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PsyRecommendationRepository extends JpaRepository<PsyRecommendation, Long> {
    Optional<PsyRecommendation> findBySession_IdAndAthlete_Id(Long sessionId, Long athleteId);

    @Modifying
    @Query("delete from PsyRecommendation r where r.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);
}
