package com.simada_backend.repository.coach;

import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.model.athlete.AthletePerformanceSnapshot;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RankingRepository extends CrudRepository<AthletePerformanceSnapshot, Integer> {

    Optional<AthletePerformanceSnapshot> findTopByAthleteIdOrderByAsOfDesc(Long athleteId);

    @Query(value = """
            SELECT s.*
            FROM athlete_performance_snapshot s
            JOIN (
              SELECT athlete_id, MAX(as_of) AS max_as_of
              FROM athlete_performance_snapshot
              WHERE coach_id = :coachId
              GROUP BY athlete_id
            ) t ON t.athlete_id = s.athlete_id AND t.max_as_of = s.as_of
            WHERE s.coach_id = :coachId
            """, nativeQuery = true)
    List<AthletePerformanceSnapshot> findCoachLatestSnapshots(@Param("coachId") Long coachId);

    @Query(value = """
            WITH ranked AS (
              SELECT
                s.athlete_id,
                s.points,
                s.as_of,
                ROW_NUMBER() OVER (PARTITION BY s.athlete_id ORDER BY s.as_of DESC) AS rn,
                LAG(s.points) OVER (PARTITION BY s.athlete_id ORDER BY s.as_of DESC) AS last_points
              FROM athlete_performance_snapshot s
            ),
            latest AS (
              SELECT
                r.athlete_id,
                r.points,
                r.last_points,
                r.as_of
              FROM ranked r
              WHERE r.rn = 1
            )
            SELECT
              a.name                                  AS nome_atleta,
              u.photo                                 AS foto,
              l.as_of                                 AS data_atualizacao,
              CAST(l.points AS DOUBLE)                AS pontuacao,
              CAST(COALESCE(l.last_points, 0) AS DOUBLE) AS ultima_pontuacao
            FROM latest l
            JOIN athlete a ON a.id = l.athlete_id
            LEFT JOIN `user` u ON u.id = a.id_user
            ORDER BY l.points DESC, l.as_of DESC, l.athlete_id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopPerformerRow> findTopPerformers(@Param("limit") int limit);

    public interface TopPerformerRow {
        String getNome_atleta();

        String getFoto();

        LocalDateTime getData_atualizacao();

        Double getPontuacao();

        Double getUltima_pontuacao();
    }
}