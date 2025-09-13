package com.simada_backend.repository.session;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    @Query(value = "SELECT s.id_coach FROM session s WHERE s.id = :sessionId", nativeQuery = true)
    Long findCoachIdBySessionId(Long sessionId);

    // sessão “lógica” por coach+data+título
    List<Session> findAllByCoach_IdAndDateAndTitle(Long coachId, LocalDate date, String title);

    // fallback quando o CSV vier sem título (title vazio/nulo)
    List<Session> findAllByCoach_IdAndDate(Long coachId, LocalDate date);
}
