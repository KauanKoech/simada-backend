package com.simada_backend.repository.session;

import com.simada_backend.model.session.Metrics;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetricsRepository extends JpaRepository<Metrics, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM Metrics m WHERE m.session.id = :sessionId")
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    @Query(value = "SELECT DISTINCT m.id_athlete FROM metrics m WHERE m.id_session = :sessionId", nativeQuery = true)
    List<Long> findAthletesBySessionId(@Param("sessionId") Long sessionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Metrics m where m.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);
}