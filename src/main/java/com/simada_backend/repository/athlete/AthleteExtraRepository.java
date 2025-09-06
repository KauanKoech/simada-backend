package com.simada_backend.repository.athlete;

import com.simada_backend.model.athlete.AthleteExtra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AthleteExtraRepository extends JpaRepository<AthleteExtra, Long> {
    boolean existsByAthlete_Id(Long athleteId);
    Optional<AthleteExtra> findByAthlete_Id(Long athleteId);
}