package com.simada_backend.repository.session;

import com.simada_backend.dto.response.sessionGraphs.AthleteListRow;
import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionMetricsQueryRepository extends Repository<Session, Long> {

    interface AthleteListRow {
        Long getId();

        String getName();

        String getPosition();

        Integer getJersey_number();

        String getAvatar_url();
    }

    interface MetricsRowView {
        Long getId();

        Long getId_athlete();

        String getPlayer();

        java.time.LocalDate getDate();     // LocalDate ok

        Double getTotal_distance();

        Integer getSprints();

        Integer getNum_acc_expl();

        Double getAverage_speed();

        Double getPlayer_load();

        Double getHsr();

        Double getMax_speed();

        Double getDistance_vrange1();

        Double getDistance_vrange2();

        Double getDistance_vrange3();

        Double getDistance_vrange4();

        Double getDistance_vrange5();

        Double getDistance_vrange6();

        Double getTime();
    }

    @Query(value = """
            SELECT
              CAST(a.id AS SIGNED)                 AS id,
              a.name                               AS name,
              a.position                           AS position,
              a.jersey_number                      AS jersey_number,
              u.photo                              AS avatar_url
            FROM athlete a
            JOIN metrics m   ON m.id_athlete = a.id
            JOIN session s   ON s.id = m.id_session
            LEFT JOIN `user` u ON u.id = a.id_user
            WHERE s.id = :sessionId
            GROUP BY a.id, a.name, a.position, a.jersey_number, u.photo
            ORDER BY a.name ASC
            """, nativeQuery = true)
    List<AthleteListRow> listAthletesInSession(@Param("sessionId") Long sessionId);

    @Query(value = """
            SELECT
              CAST(m.id AS SIGNED)                 AS id,
              CAST(a.id AS SIGNED)                 AS id_athlete,
              a.name                               AS player,
              DATE(s.date)                         AS date,
              m.total_distance                     AS total_distance,
              m.sprints                            AS sprints,
              m.num_acc_expl                       AS num_acc_expl,
              m.average_speed                      AS average_speed,
              m.player_load                        AS player_load,
              m.hsr                                AS hsr,
              m.max_speed                          AS max_speed,
              m.distance_vrange1                   AS distance_vrange1,
              m.distance_vrange2                   AS distance_vrange2,
              m.distance_vrange3                   AS distance_vrange3,
              m.distance_vrange4                   AS distance_vrange4,
              m.distance_vrange5                   AS distance_vrange5,
              m.distance_vrange6                   AS distance_vrange6,
              m.time                               AS time
            FROM metrics m
            JOIN athlete a ON a.id = m.id_athlete
            JOIN session s ON s.id = m.id_session
            WHERE s.id = :sessionId
            ORDER BY a.name ASC
            """, nativeQuery = true)
    List<MetricsRowView> listMetricsForTeam(@Param("sessionId") Long sessionId);

    @Query(value = """
            SELECT
              CAST(m.id AS SIGNED)                 AS id,
              CAST(a.id AS SIGNED)                 AS id_athlete,
              a.name                               AS player,
              DATE(s.date)                         AS date,
              m.total_distance                     AS total_distance,
              m.sprints                            AS sprints,
              m.num_acc_expl                       AS num_acc_expl,
              m.average_speed                      AS average_speed,
              m.player_load                        AS player_load,
              m.hsr                                AS hsr,
              m.max_speed                          AS max_speed,
              m.distance_vrange1                   AS distance_vrange1,
              m.distance_vrange2                   AS distance_vrange2,
              m.distance_vrange3                   AS distance_vrange3,
              m.distance_vrange4                   AS distance_vrange4,
              m.distance_vrange5                   AS distance_vrange5,
              m.distance_vrange6                   AS distance_vrange6,
              m.time                               AS time
            FROM metrics m
            JOIN athlete a ON a.id = m.id_athlete
            JOIN session s ON s.id = m.id_session
            WHERE s.id = :sessionId
              AND a.id = :athleteId
            """, nativeQuery = true)
    List<MetricsRowView> listMetricsForAthlete(@Param("sessionId") Long sessionId,
                                               @Param("athleteId") Long athleteId);
}