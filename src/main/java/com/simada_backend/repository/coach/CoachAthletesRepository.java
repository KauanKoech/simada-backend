package com.simada_backend.repository.coach;

import com.simada_backend.model.athlete.Athlete;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoachAthletesRepository extends JpaRepository<Athlete, Long> {

    @Query(value = """
            SELECT
              a.id               AS id,
              a.name             AS name,
              u.email            AS email,
              u.birth_date       AS birth,
              u.phone            AS phone,
              a.jersey_number    AS jersey_number,
              a.position         AS position,
              u.photo            AS photo
            FROM athlete a
            LEFT JOIN user u ON u.id = a.id_user
            WHERE a.id_coach = :coachId
              AND (:q IS NULL OR :q = '' 
                   OR a.name  LIKE CONCAT('%', :q, '%')
                   OR u.email LIKE CONCAT('%', :q, '%'))
            ORDER BY a.name ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<AthleteRow> findAthletes(
            @Param("coachId") int coachId,
            @Param("q") String q,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    interface AthleteRow {
        Long getId();

        String getName();

        String getEmail();

        java.time.LocalDate getBirth();

        String getPhone();

        Integer getJersey_number();

        String getPosition();

        String getPhoto();
    }
}