package com.simada_backend.repository.psycho;

import com.simada_backend.model.psycho.PsychoAlert;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PsychoAlertRepository extends JpaRepository<PsychoAlert, Long> {

    interface AlertCardRow {
        Long getAlert_id();

        Long getAthlete_id();

        String getAthlete_name();

        String getAthlete_photo();

        Long getSession_id();

        Long getAnswer_id();

        Integer getSrpe();

        Integer getFatigue();

        Integer getSoreness();

        Integer getMood();

        Integer getEnergy();

        Integer getTotal();

        String getRisk();

        java.time.LocalDate getDate();
    }

    interface PsyAnswerRow {
        Long getAnswerId();

        Long getAthleteId();

        String getAthleteName();

        String getAthleteEmail();

        String getAthlete_position();

        String getAthletePhoto();

        LocalDateTime getSubmittedAt();

        Integer getSrpe();

        Integer getFatigue();

        Integer getSoreness();

        Integer getMood();

        Integer getEnergy();

        String getToken();
    }

    @Query(value = """
            SELECT
              pa.id              AS alert_id,
              pa.athlete_id      AS athlete_id,
              a.name             AS athlete_name,
              u.photo            AS athlete_photo,
              pa.session_id      AS session_id,
              pa.answer_id       AS answer_id,
            
              pfa.srpe           AS srpe,
              pfa.fatigue        AS fatigue,
              pfa.soreness       AS soreness,
              pfa.mood           AS mood,
              pfa.energy         AS energy,
            
              prs.total_score    AS total,
              prs.risk_level     AS risk,
              DATE(pa.created_at) AS date
            FROM psycho_alert pa
            JOIN psycho_risk_score prs ON prs.answer_id = pa.answer_id
            JOIN psycho_form_answer pfa ON pfa.id = pa.answer_id
            JOIN athlete a ON a.id = pa.athlete_id
            JOIN `user` u ON u.id = a.id_user
            WHERE pa.coach_id = :coachId
              AND pa.resolved_at IS NULL
            ORDER BY pa.created_at DESC
            """, nativeQuery = true)
    List<AlertCardRow> findOpenCardsByCoach(@Param("coachId") Long coachId);

    @Query(value = """
            SELECT
              pfa.id               AS answerId,
              a.id                 AS athleteId,
              a.name               AS athleteName,
              u.email              AS athleteEmail,
              a.position           AS athlete_position,
              u.photo              AS athletePhoto,
              pfa.submitted_at     AS submittedAt,
              pfa.srpe             AS srpe,
              pfa.fatigue          AS fatigue,
              pfa.soreness         AS soreness,
              pfa.mood             AS mood,
              pfa.energy           AS energy,
              pfi.token            AS token
            FROM psycho_alert pa
            JOIN psycho_form_answer pfa ON pfa.id = pa.answer_id
            JOIN athlete a              ON a.id  = pa.athlete_id
            JOIN `user` u               ON u.id  = a.id_user
            LEFT JOIN psycho_form_invite pfi
                   ON pfi.id_session = pa.session_id
                  AND pfi.id_athlete = pa.athlete_id
                  AND pfi.id_coach   = pa.coach_id
            WHERE pa.session_id = :sessionId
              AND pa.athlete_id = :athleteId
              AND pa.resolved_at IS NULL
            ORDER BY pfa.submitted_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<PsyAnswerRow> findLatestOpenPsyAnswerBySessionAndAthlete(
            @Param("sessionId") Long sessionId,
            @Param("athleteId") Long athleteId
    );

    @Modifying
    @Query("UPDATE PsychoAlert a SET a.resolvedAt = :resolvedAt WHERE a.id = :id")
    int resolve(@Param("id") Long id, @Param("resolvedAt") LocalDateTime resolvedAt);

    boolean existsByAnswerIdAndRuleCode(Long answerId, String ruleCode);
}
