package com.simada_backend.repository.psycho;

import com.simada_backend.model.psycho.PsychoRiskScore;
import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PsychoRiskScoreRepository extends JpaRepository<PsychoRiskScore, Long> {

    boolean existsByAnswerId(Long answerId);

    @Query(value = """
            SELECT
              prs.athlete_id      AS athlete_id,
              prs.session_id      AS session_id,
              prs.answer_id       AS answer_id,
              prs.total_score     AS total_score,
              prs.risk_level      AS risk_level,
              a.name              AS athlete_name,
              u.photo             AS athlete_photo,
              DATE(prs.created_at) AS date
            FROM psycho_risk_score prs
            JOIN athlete a ON a.id = prs.athlete_id
            JOIN `user` u  ON u.id = a.id_user
            WHERE prs.coach_id = :coachId
              AND prs.risk_level IN ('MODERATE','HIGH')
            ORDER BY prs.created_at DESC
            """, nativeQuery = true)
    List<RiskCardRow> findRiskCardsByCoach(@Param("coachId") Long coachId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        DELETE prs
        FROM psycho_risk_score prs
        JOIN psycho_form_answer ans ON prs.answer_id = ans.id
        WHERE ans.id_athlete = :athleteId
        """, nativeQuery = true)
    void deleteByAnswerAthleteId(@Param("athleteId") Long athleteId);

    @Modifying
    @Query("delete from PsyRecommendation r where r.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PsychoRiskScore pr where pr.session.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);

    List<PsychoRiskScore> session(Session session);

    interface RiskCardRow {
        Long getAthlete_id();

        Long getSession_id();

        Long getAnswer_id();

        Integer getTotal_score();

        String getRisk_level();

        String getAthlete_name();

        String getAthlete_photo();

        java.time.LocalDate getDate();
    }
}