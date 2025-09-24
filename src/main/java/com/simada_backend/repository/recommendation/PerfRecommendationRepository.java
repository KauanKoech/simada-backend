package com.simada_backend.repository.recommendation;

import com.simada_backend.model.recommendation.PerfRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PerfRecommendationRepository extends JpaRepository<PerfRecommendation, Long> {
    Optional<PerfRecommendation> findBySession_IdAndAthlete_Id(Long sessionId, Long athleteId);

    @Modifying
    @Query("delete from PerfRecommendation r where r.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);
}

