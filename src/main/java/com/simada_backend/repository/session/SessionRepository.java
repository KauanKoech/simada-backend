package com.simada_backend.repository.session;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    @Query(value = "SELECT s.id_coach FROM session s WHERE s.id = :sessionId", nativeQuery = true)
    Long findCoachIdBySessionId(Long sessionId);
}
