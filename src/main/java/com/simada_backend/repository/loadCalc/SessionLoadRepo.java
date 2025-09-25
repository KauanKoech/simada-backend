package com.simada_backend.repository.loadCalc;

import com.simada_backend.model.loadCalc.SessionLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionLoadRepo extends JpaRepository<SessionLoad, Long> {
    Optional<SessionLoad> findBySessionIdAndAthleteId(Long sessionId, Long athleteId);

    @Modifying
    @Query("delete from SessionLoad s where s.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SessionLoad sl where sl.session.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}
