package com.simada_backend.repository.session;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public interface AthleteSessionRepository extends Repository<Session, Long> {
    @Query(value = """
            SELECT DISTINCT
                   s.id                             AS id,
                   s.coach_photo                    AS coachPhoto,
                   s.`date`                         AS `date`,
                   s.session_type                   AS `type`,
                   s.title                          AS title,
                   s.num_athletes                   AS athleteCount,
                   s.score                          AS score,
                   s.description                    AS description,
                   s.local                          AS `location`,
                   CASE WHEN EXISTS (
                        SELECT 1 FROM metrics mx
                        WHERE mx.id_session = s.id AND mx.id_athlete = :athleteId
                   ) THEN 1 ELSE 0 END              AS has_metrics
            FROM `session` s
            JOIN metrics m ON m.id_session = s.id AND m.id_athlete = :athleteId
            ORDER BY s.`date` DESC, s.id DESC
            """, nativeQuery = true)
    List<AthleteSessionProjection> findAthleteSessions(Long athleteId);

    interface AthleteSessionProjection {
        Long getId();
        String getCoachPhoto();
        java.time.LocalDate getDate();
        String getType();
        String getTitle();
        Integer getAthleteCount();
        String getScore();
        String getDescription();
        String getLocation();
        Integer getHas_metrics();
    }
}
