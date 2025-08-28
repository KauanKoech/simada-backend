// src/main/java/com/simada_backend/repository/trainer/TrainerSessionRepository.java
package com.simada_backend.repository.trainer;

import com.simada_backend.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainerSessionsRepository extends JpaRepository<Sessao, Integer> {

    @Query(value = """
            SELECT
              s.id_sessao          AS id,
              s.id_treinador       AS trainer_id,
              s.foto_treinador     AS trainer_photo,
              s.data_hora_inicio   AS start,
              s.data_hora_termino  AS end,
              s.tipo_sessao        AS type,
              s.descricao          AS description,
              s.local              AS location,
              s.titulo             AS title,
              s.atletas            AS athleteCount,
              s.placar             AS score,
            FROM sessao s
            WHERE s.id_treinador = :trainerId
              AND (:from IS NULL OR s.data_hora_inicio >= :from)
              AND (:to   IS NULL OR s.data_hora_inicio <  :to)
            ORDER BY s.data_hora_inicio DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TrainerSessionRow> findSessions(
            @Param("trainerId") int trainerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("limit") int limit
    );

    interface TrainerSessionRow {
        Long getId();
        Long getTrainer_id();
        String getTrainer_photo();
        LocalDateTime getStart();
        LocalDateTime getEnd();
        String getType();
        String getDescription();
        String getLocation();
        String getTitle();
        String getScore();
        String getAthleteCount();
    }
}
