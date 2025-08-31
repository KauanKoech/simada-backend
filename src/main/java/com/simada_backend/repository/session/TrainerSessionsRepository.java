package com.simada_backend.repository.session;

import com.simada_backend.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainerSessionsRepository extends JpaRepository<Sessao, Integer> {

    @Query(value = """
        SELECT
          s.id_sessao        AS id,
          s.id_treinador     AS trainer_id,
          s.foto_treinador   AS trainer_photo,
          s.data             AS date,
          s.tipo_sessao      AS type,
          s.titulo           AS title,
          s.num_atletas      AS athletes_count,
          s.placar           AS score,
          s.descricao        AS description,
          s.local            AS location,
          CASE WHEN COUNT(m.id_metricas) > 0 THEN 1 ELSE 0 END AS has_metrics   
        FROM sessao s
        LEFT JOIN metricas m ON m.id_sessao = s.id_sessao
        WHERE s.id_treinador = :trainerId
          AND (:fromDate IS NULL OR s.data >= :fromDate)
          AND (:toDate   IS NULL OR s.data <  :toDate)
        GROUP BY
          s.id_sessao, s.id_treinador, s.foto_treinador, s.data, s.tipo_sessao,
          s.titulo, s.num_atletas, s.placar, s.descricao, s.local
        ORDER BY s.data DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SessionRow> findSessions(
            @Param("trainerId") long trainerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("limit") int limit
    );

    interface SessionRow {
        Long getId();
        Long getTrainer_id();
        String getTrainer_photo();
        LocalDate getDate();
        String getType();
        String getTitle();
        Integer getAthletes_count();
        String getScore();
        String getDescription();
        String getLocation();
        Integer getHas_metrics();
    }
}
