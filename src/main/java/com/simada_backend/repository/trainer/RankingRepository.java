package com.simada_backend.repository.trainer;

import com.simada_backend.model.Atleta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RankingRepository extends Repository<Atleta, Integer> {

    @Query(value = """
            WITH ranked AS (
              SELECT
                m.id_atleta,
                g.pontuacao,
                g.data_atualizacao,
                LAG(g.pontuacao) OVER (
                  PARTITION BY m.id_atleta
                  ORDER BY g.data_atualizacao DESC, g.id_ranking DESC
                ) AS ultima_pontuacao,
                ROW_NUMBER() OVER (
                  PARTITION BY m.id_atleta
                  ORDER BY g.data_atualizacao DESC, g.id_ranking DESC
                ) AS rn
              FROM gamificacao_ranking g
              JOIN metricas m ON m.id_metricas = g.id_metricas
            )
            SELECT
              a.nome                              AS nome_atleta,
              a.foto                              AS foto,
              r.data_atualizacao                  AS data_atualizacao,
              r.pontuacao                         AS pontuacao,
              r.ultima_pontuacao                  AS ultima_pontuacao
            FROM ranked r
            JOIN atleta a ON a.id_atleta = r.id_atleta
            WHERE r.rn = 1
            ORDER BY r.pontuacao DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopPerformerRow> findTopPerformers(@Param("limit") int limit
            /*, @Param("trainerId") Integer trainerId */);

    interface TopPerformerRow {
        String getNome_atleta();

        String getFoto();

        LocalDateTime getData_atualizacao();

        Double getPontuacao();

        Double getUltima_pontuacao();
    }
}