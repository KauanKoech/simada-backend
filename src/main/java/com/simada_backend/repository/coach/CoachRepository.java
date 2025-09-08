package com.simada_backend.repository.coach;

import com.simada_backend.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    @Query(value = """
              SELECT c.* FROM coach c
              JOIN `user` u ON u.id = c.id_user
              WHERE c.id_user = :id
            """, nativeQuery = true)
    Optional<Coach> findByIdWithUser(@Param("id") Long id);

    @Query(value = "SELECT u.id FROM `user` u WHERE LOWER(u.email) = LOWER(:email) LIMIT 1", nativeQuery = true)
    Optional<Long> findUserIdByEmail(@Param("email") String email);

    @Modifying
    @Query(value = """
            INSERT INTO coach (id, id_user, name, team)
            SELECT u.id, u.id, u.name, NULL
            FROM `user` u
            WHERE u.id = :userId
              AND NOT EXISTS (SELECT 1 FROM coach c WHERE c.id = :userId)
            """, nativeQuery = true)
    int ensureCoachExistsForUser(@Param("userId") Long userId);

    @Query(value = "SELECT id FROM coach WHERE id_user = :userId LIMIT 1", nativeQuery = true)
    Optional<Long> findCoachIdByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT id_user FROM coach WHERE id = :coachId", nativeQuery = true)
    Optional<Long> findUserIdByCoachId(@Param("coachId") Long coachId);

    @Query(value = "SELECT u.name FROM `user` u WHERE u.id = :userId", nativeQuery = true)
    Optional<String> findUserNameById(@Param("userId") Long userId);

    @Modifying
    @Query(value = "UPDATE athlete           SET id_coach = :toId WHERE id_coach = :fromId", nativeQuery = true)
    int reassignAthlete(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Modifying
    @Query(value = "UPDATE athlete_invite     SET id_coach = :toId WHERE id_coach = :fromId", nativeQuery = true)
    int reassignAthleteInvite(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Modifying
    @Query(value = "UPDATE session            SET id_coach = :toId WHERE id_coach = :fromId", nativeQuery = true)
    int reassignSession(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Modifying
    @Query(value = "UPDATE psycho_alert       SET coach_id = :toId WHERE coach_id = :fromId", nativeQuery = true)
    int reassignPsychoAlert(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Modifying
    @Query(value = "UPDATE psycho_risk_score  SET coach_id = :toId WHERE coach_id = :fromId", nativeQuery = true)
    int reassignPsychoRiskScore(@Param("fromId") Long fromId, @Param("toId") Long toId);

    @Modifying
    @Query(value = """
            UPDATE psycho_form_invite
            SET id_coach = :destCoachId
            WHERE id_coach = :sourceCoachId
            """, nativeQuery = true)
    int reassignPsychoFormInvite(@Param("sourceCoachId") Long sourceCoachId,
                                 @Param("destCoachId") Long destCoachId);
    // DELETE
    /* -------- PSICO (depende de answer / session / athlete) -------- */

    // 1) Alertas (referenciam answer_id) – apague antes de answers
    @Modifying
    @Query(value = "DELETE FROM psycho_alert WHERE coach_id = :coachId", nativeQuery = true)
    int deletePsychoAlertsByCoach(@Param("coachId") Long coachId);

    // 2) Scores (referenciam answer_id) – apague antes de answers
    @Modifying
    @Query(value = "DELETE FROM psycho_risk_score WHERE coach_id = :coachId", nativeQuery = true)
    int deletePsychoRiskScoresByCoach(@Param("coachId") Long coachId);

    // 3) Answers – NÃO existe id_invite; delete por sessão e/ou atleta do coach
    @Modifying
    @Query(value = """
            DELETE FROM psycho_form_answer
            WHERE id_session IN (SELECT s.id FROM session s WHERE s.id_coach = :coachId)
               OR id_athlete IN (SELECT a.id FROM athlete a WHERE a.id_coach = :coachId)
            """, nativeQuery = true)
    int deletePsychoFormAnswersByCoach(@Param("coachId") Long coachId);

    // 4) Invites psico (tem id_coach direto)
    @Modifying
    @Query(value = "DELETE FROM psycho_form_invite WHERE id_coach = :coachId", nativeQuery = true)
    int deletePsychoFormInvitesByCoach(@Param("coachId") Long coachId);


    /* --------------------- SESSÕES E MÉTRICAS ---------------------- */

    // 5) Métricas por sessão do coach (metrics não tem id_coach)
    @Modifying
    @Query(value = """
            DELETE FROM metrics
            WHERE id_session IN (SELECT s.id FROM session s WHERE s.id_coach = :coachId)
            """, nativeQuery = true)
    int deleteMetricsByCoachViaSessions(@Param("coachId") Long coachId);

    // 6) Sessões do coach
    @Modifying
    @Query(value = "DELETE FROM session WHERE id_coach = :coachId", nativeQuery = true)
    int deleteSessionsByCoach(@Param("coachId") Long coachId);


    /* ---------------------- ATLETAS E EXTRAS ----------------------- */

    @Modifying
    @Query(value = """
            DELETE ae FROM athlete_extra ae
            JOIN athlete a ON a.id = ae.id_athlete
            WHERE a.id_coach = :coachId
            """, nativeQuery = true)
    int deleteAthleteExtrasByCoach(@Param("coachId") Long coachId);

    @Modifying
    @Query(value = "DELETE FROM athlete_invite WHERE id_coach = :coachId", nativeQuery = true)
    int deleteAthleteInvitesByCoach(@Param("coachId") Long coachId);

    @Query(value = "SELECT a.id_user FROM athlete a WHERE a.id_coach = :coachId", nativeQuery = true)
    List<Long> findAthleteUserIdsByCoach(@Param("coachId") Long coachId);

    @Modifying
    @Query(value = "DELETE FROM athlete WHERE id_coach = :coachId", nativeQuery = true)
    int deleteAthletesByCoach(@Param("coachId") Long coachId);

    @Modifying
    @Query(value = """
            DELETE u FROM `user` u
            WHERE u.id IN (SELECT a.id_user FROM athlete a WHERE a.id_coach = :coachId)
            """, nativeQuery = true)
    int deleteUsersOfAthletesByCoach(@Param("coachId") Long coachId);


    /* -------------------------- COACH/USER ------------------------- */

    @Modifying
    @Query(value = """
            DELETE u FROM `user` u
            WHERE u.name = :coachName
            """, nativeQuery = true)
    int deleteUserByCoachId(@Param("coachName") String coachName);

    @Modifying
    @Query(value = "DELETE FROM coach WHERE id = :coachId", nativeQuery = true)
    int deleteCoachRow(@Param("coachId") Long coachId);
}
