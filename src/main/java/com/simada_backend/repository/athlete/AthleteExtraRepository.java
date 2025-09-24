package com.simada_backend.repository.athlete;

import com.simada_backend.model.athlete.AthleteExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AthleteExtraRepository extends JpaRepository<AthleteExtra, Long> {
    Optional<AthleteExtra> findByAthlete_Id(Long athleteId);

    @Modifying
    @Query("delete from AthleteExtra e where e.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);
}