package com.simada_backend.repository.psycho;

import com.simada_backend.model.psycho.PsychoFormAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PsychoFormAnswerRepository extends JpaRepository<PsychoFormAnswer, Long> {

    @Query(value = """
            SELECT
              pfa.id                                   AS id,
              pfa.id_session                           AS id_session,
              pfa.id_athlete                           AS id_athlete,
              pfa.token                                AS token,
              DATE(pfa.submitted_at)                   AS submitted_at,
              pfa.srpe                                 AS srpe,
              pfa.fatigue                              AS fatigue,
              pfa.soreness                             AS soreness,
              pfa.mood                                 AS mood,
              pfa.energy                               AS energy,
              a.name                                   AS athlete_name,
              a.position                               AS athlete_position,
              u.email                                  AS athlete_email,
              u.photo                                  AS athlete_photo
            FROM psycho_form_answer pfa
            JOIN athlete a ON a.id = pfa.id_athlete
            JOIN `user` u ON u.id = a.id_user
            WHERE pfa.id_session = :sessionId
            ORDER BY pfa.submitted_at DESC
            """, nativeQuery = true)
    List<PsychoAnswerRow> findAnswersBySession(@Param("sessionId") Long sessionId);

    interface PsychoAnswerRow {
        Long getId();

        Long getId_session();

        Long getId_athlete();

        String getToken();

        LocalDate getSubmitted_at();

        Integer getSRPE();

        Integer getFatigue();

        Integer getSoreness();

        Integer getMood();

        Integer getEnergy();

        String getAthlete_name();

        String getAthlete_email();

        String getAthlete_photo();

        String getAthlete_position();
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PsychoFormAnswer a where a.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PsychoFormAnswer pfa where pfa.idSession.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}