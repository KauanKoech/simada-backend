package com.simada_backend.repository.session;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CoachSessionsRepository extends JpaRepository<Session, Integer> {

    @Query(value = """
        SELECT
          s.id               AS id,
          s.id_coach         AS coach_id,
          s.coach_photo      AS coach_photo,
          s.date             AS date,
          s.session_type     AS type,
          s.title            AS title,
          s.num_athletes     AS athletes_count,
          s.score            AS score,
          s.description      AS description,
          s.local            AS local,
          CASE WHEN COUNT(DISTINCT m.id) > 0 THEN 1 ELSE 0 END AS has_metrics,
          CASE WHEN EXISTS (SELECT 1 FROM psycho_form_invite p WHERE p.id_session = s.id) THEN 1 ELSE 0 END AS has_psycho
        FROM session s
        LEFT JOIN metrics m ON m.id_session = s.id
        WHERE s.id_coach = :coachId
          AND (:fromDate IS NULL OR s.date >= :fromDate)
          AND (:toDate   IS NULL OR s.date <  :toDate)
        GROUP BY
          s.id, s.id_coach, s.coach_photo, s.date, s.session_type,
          s.title, s.num_athletes, s.score, s.description, s.local
        ORDER BY s.date DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SessionRow> findSessions(
            @Param("coachId") long coachId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("limit") int limit
    );

    public interface SessionRow {
        Long getId();
        Long getCoach_id();
        String getCoach_photo();
        LocalDate getDate();
        String getType();
        String getTitle();
        Integer getAthletes_count();
        String getScore();
        String getDescription();
        String getLocal();
        Integer getHas_metrics();
        Integer getHas_psycho();
    }

}
