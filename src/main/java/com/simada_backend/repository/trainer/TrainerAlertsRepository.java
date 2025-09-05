package com.simada_backend.repository.trainer;

import com.simada_backend.model.session.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainerAlertsRepository extends JpaRepository<Sessao, Integer> {

    @Query(value = """
            SELECT
              alet.id_alerta            AS id,
              alet.data_alerta          AS date,
              alet.tipo_alerta          AS type,
              alet.mensagem_alerta      AS message,
              alet.status_alerta        AS status,
              alet.acao_sugerida        AS action,
              atl.nome                  AS athlete_name,
              usr.foto                  AS athlete_photo,
            
              -- PERFORMANCE
              alet.valor_anterior       AS prev_value,
              alet.valor_atual          AS curr_value,
              alet.percentual           AS percent,
              alet.unidade              AS unit,
            
              -- PSICO (pegas do questionário mais recente ligado à mesma métrica)
              /* Ajuste os rótulos 'fatigue','mood','hours_slept' para os textos das suas perguntas */
              CASE WHEN alet.tipo_alerta = 'PSYCHO' THEN (
                SELECT rq.resposta_texto
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
            
            FROM alertas alet
            JOIN metricas m       ON m.id_metricas   = alet.id_metricas
            JOIN atleta   atl     ON atl.id_atleta   = m.id_atleta
            LEFT JOIN usuario usr ON usr.id_usuario  = atl.id_usuario
            WHERE atl.id_treinador = :trainerId
              AND alet.data_alerta >= DATE_SUB(NOW(), INTERVAL :days DAY)
              AND (:category IS NULL OR alet.tipo_alerta = :category)
            ORDER BY alet.data_alerta DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<AlertRow> findTrainerAlerts(@Param("trainerId") int trainerId,
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
