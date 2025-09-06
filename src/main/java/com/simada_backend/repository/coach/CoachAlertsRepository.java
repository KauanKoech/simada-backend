package com.simada_backend.repository.coach;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CoachAlertsRepository extends JpaRepository<Session, Integer> {

    @Query(value = """
            SELECT
              alet.alert_id             AS id,
              alet.alert_Date           AS date,
              alet.alert_type           AS type,
              alet.alert_message        AS message,
              alet.alert_status         AS status,
              alet.suggested_action     AS action,
              atl.name                  AS athlete_name,
              usr.photo                 AS athlete_photo,
            
              -- PERFORMANCE
              alet.prev_value          AS prev_value,
              alet.curr_value          AS curr_value,
              alet.percent             AS percent,
              alet.unit                AS unit,
            
              -- PSICO (pegas do questionário mais recente ligado à mesma métrica)
              /* Ajuste os rótulos 'fatigue','mood','hours_slept' para os textos das suas perguntas */
              CASE WHEN alet.alert_type = 'PSYCHO' THEN (
                SELECT rq.answer_text
                FROM resposta_questionario rq
                JOIN questionario q2 ON q2.id_questionario = rq.id_questionario
                WHERE q2.id_metricas = m.id_metricas AND rq.pergunta = 'fatigue'
                ORDER BY rq.id_resposta DESC LIMIT 1
              ) ELSE NULL END AS fatigue,
            
              CASE WHEN alet.tipo_alerta = 'PSYCHO' THEN (
                SELECT rq.resposta_texto
                FROM resposta_questionario rq
                JOIN questionario q2 ON q2.id_questionario = rq.id_questionario
                WHERE q2.id_metricas = m.id_metricas AND rq.pergunta = 'mood'
                ORDER BY rq.id_resposta DESC LIMIT 1
              ) ELSE NULL END AS mood,
            
              CASE WHEN alet.tipo_alerta = 'PSYCHO' THEN (
                SELECT CAST(rq.resposta_numerica AS SIGNED)
                FROM resposta_questionario rq
                JOIN questionario q2 ON q2.id_questionario = rq.id_questionario
                WHERE q2.id_metricas = m.id_metricas AND rq.pergunta = 'hours_slept'
                ORDER BY rq.id_resposta DESC LIMIT 1
              ) ELSE NULL END AS hours_slept
            
            FROM alerts alet
            JOIN metrics m       ON m.id   = alet.id_metrics
            JOIN athlete   atl     ON atl.id   = m.id_athlete
            LEFT JOIN user usr ON usr.id  = atl.id_user
            WHERE atl.id_coach = :coachId
              AND alet.alert_date >= DATE_SUB(NOW(), INTERVAL :days DAY)
              AND (:category IS NULL OR alet.alert_type = :category)
            ORDER BY alet.alert_date DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AlertRow> findCoachAlerts(@Param("coachId") int coachId,
                                     @Param("days") int days,
                                     @Param("limit") int limit,
                                     @Param("category") String category);

    interface AlertRow {
        Long getId();
        LocalDateTime getDate();
        String getType();
        String getMessage();
        String getStatus();
        String getAction();
        String getAthlete_name();
        String getAthlete_photo();

        // PERFORMANCE
        Double getPrev_value();
        Double getCurr_value();
        Double getPercent();
        String getUnit();

        // PSICO
        String getFatigue();
        String getMood();
        Integer getHours_slept();
    }
}
