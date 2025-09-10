package com.simada_backend.repository.athlete;

import com.simada_backend.model.athlete.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeerAthleteRepository extends JpaRepository<Athlete, Long> {

    @Query(value = """
            SELECT
                a.id                                      AS id,
                COALESCE(a.name, u.name)                  AS name,
                u.email                                   AS email,
                a.position                                AS position,
                CAST(a.jersey_number AS CHAR)             AS jersey,
                u.photo                                   AS avatar,
                perf.points                               AS points,
                ax.nationality                            AS nationality
            FROM athlete a
            JOIN athlete a_self 
              ON a_self.id = :athleteId 
             AND a.id_coach = a_self.id_coach
            LEFT JOIN `user` u 
              ON u.id = a.id_user
            LEFT JOIN athlete_extra ax
              ON ax.id_athlete = a.id
            LEFT JOIN (
                SELECT aps1.athlete_id, aps1.points
                FROM athlete_performance_snapshot aps1
                JOIN (
                    SELECT athlete_id, MAX(as_of) AS max_as_of
                    FROM athlete_performance_snapshot
                    GROUP BY athlete_id
                ) last 
                  ON last.athlete_id = aps1.athlete_id 
                 AND last.max_as_of  = aps1.as_of
            ) perf 
              ON perf.athlete_id = a.id
            ORDER BY a.name ASC, a.id ASC
            """, nativeQuery = true)
    List<PeerProjection> findPeersByAthlete(Long athleteId);


    interface PeerProjection {
        Long getId();

        String getName();

        String getEmail();

        String getPosition();

        String getJersey();

        String getNationality();

        String getAvatar();

        Integer getPoints(); // pode vir null se ainda não houver snapshot
    }
}
