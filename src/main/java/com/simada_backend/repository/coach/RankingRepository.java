package com.simada_backend.repository.coach;

import com.simada_backend.model.athlete.Athlete;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RankingRepository extends Repository<Athlete, Integer> {

    @Query(value = """
            WITH ranked AS (
              SELECT
                m.id_athlete,
                g.score,
                g.update_date,
                LAG(g.score) OVER (
                  PARTITION BY m.id_athlete
                  ORDER BY g.update_date DESC, g.id_ranking DESC
                ) AS last_score,
                ROW_NUMBER() OVER (
                  PARTITION BY m.id_athlete
                  ORDER BY g.update_date DESC, g.id_ranking DESC
                ) AS rn
              FROM ranking_gamification g
              JOIN metrics m ON m.id  = g.id_metrics
            )
            SELECT
              a.name                              AS athlete_name,
              a.photo                             AS photo,
              r.update_date                       AS update_date,
              r.score                             AS update_date,
              r.last_score                        AS last_score
            FROM ranked r
            JOIN athlete a ON a.id = r.id_atleta
            WHERE r.rn = 1
            ORDER BY r.score DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopPerformerRow> findTopPerformers(@Param("limit") int limit
            /*, @Param("coachId") Integer coachId */);

    interface TopPerformerRow {
        String getAthleteName();

        String getPhoto();

        LocalDateTime getUpdateDate();

        Double getScore();

        Double getLastScore();
    }
}