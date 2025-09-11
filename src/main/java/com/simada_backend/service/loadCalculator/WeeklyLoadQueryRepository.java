package com.simada_backend.service.loadCalculator;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyLoadQueryRepository extends JpaRepository<Session, Long> {

    @Query(value = """
            SELECT COUNT(*) FROM vw_athlete_qw_load
            WHERE athlete_id = :athleteId
              AND qw_start < :fromQw
              AND ca_qw IS NOT NULL
            """, nativeQuery = true)
    int countPreviousQuads(@Param("athleteId") Long athleteId,
                           @Param("fromQw") LocalDate fromQw);

    @Query(value = "SELECT MAX(qw_start) FROM vw_athlete_qw_load WHERE athlete_id = :athleteId", nativeQuery = true)
    LocalDate findLatestQwStart(@Param("athleteId") Long athleteId);

    @Query(value = """
            WITH q AS (SELECT * FROM vw_athlete_qw_load),
            prev AS (
              SELECT
                q.*,
                LAG(ca_qw,1) OVER (PARTITION BY athlete_id ORDER BY qw_start) AS ca_prev
              FROM q
            ),
            cc AS (
              SELECT
                athlete_id, qw_start,
                (  LAG(ca_qw,1) OVER (PARTITION BY athlete_id ORDER BY qw_start)
                 + LAG(ca_qw,2) OVER (PARTITION BY athlete_id ORDER BY qw_start)
                 + LAG(ca_qw,3) OVER (PARTITION BY athlete_id ORDER BY qw_start)
                 + LAG(ca_qw,4) OVER (PARTITION BY athlete_id ORDER BY qw_start)
                ) / 4.0 AS cc_sma4
              FROM q
            )
            SELECT
              p.athlete_id        AS athleteId,
              p.qw_start          AS qwStart,
              p.ca_qw             AS ca,
              c.cc_sma4           AS cc,
              CASE WHEN c.cc_sma4 IS NULL OR c.cc_sma4<=0 THEN NULL
                   ELSE p.ca_qw / c.cc_sma4 END                        AS acwr,
              CASE WHEN p.ca_prev IS NULL OR p.ca_prev=0 THEN NULL
                   ELSE (p.ca_qw - p.ca_prev)/p.ca_prev*100 END        AS pctQwUp,
              p.mean_daily        AS meanDaily,
              p.sd_daily          AS sdDaily,
              CASE WHEN p.sd_daily IS NULL OR p.sd_daily=0 THEN NULL
                   ELSE p.mean_daily / p.sd_daily END                  AS monotony,
              CASE WHEN p.sd_daily IS NULL OR p.sd_daily=0 THEN NULL
                   ELSE p.ca_qw * (p.mean_daily / p.sd_daily) END      AS strain,
              p.days_with_load    AS daysWithLoad
            FROM prev p
            LEFT JOIN cc c
              ON c.athlete_id = p.athlete_id AND c.qw_start = p.qw_start
            WHERE p.athlete_id = :athleteId
              AND p.qw_start BETWEEN :fromQw AND :toQw
            ORDER BY p.qw_start
            """, nativeQuery = true)
    List<WeeklyQwRow> qwWindow(
            @Param("athleteId") Long athleteId,
            @Param("fromQw") LocalDate fromQw,
            @Param("toQw") LocalDate toQw
    );

    interface WeeklyQwRow {
        Long getAthleteId();

        LocalDate getQwStart();

        java.math.BigDecimal getCa();

        java.math.BigDecimal getCc();

        java.math.BigDecimal getAcwr();

        java.math.BigDecimal getPctQwUp();

        java.math.BigDecimal getMeanDaily();

        java.math.BigDecimal getSdDaily();

        java.math.BigDecimal getMonotony();

        java.math.BigDecimal getStrain();

        Integer getDaysWithLoad();
    }
}