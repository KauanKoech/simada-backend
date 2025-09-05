package com.simada_backend.repository.trainer;

import com.simada_backend.model.athlete.Atleta;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainerAthletesRepository extends JpaRepository<Atleta, Long> {

    @Query(value = """
            SELECT
              a.id_atleta        AS id,
              a.nome             AS name,
              u.email            AS email,
              u.data_nascimento  AS birth,
              u.telefone         AS phone,
              a.numero_camisa    AS shirt_number,
              a.posicao          AS position,
              u.foto             AS avatar_url
            FROM atleta a
            LEFT JOIN usuario u ON u.id_usuario = a.id_usuario
            WHERE a.id_treinador = :trainerId
              AND (:q IS NULL OR :q = '' 
                   OR a.nome  LIKE CONCAT('%', :q, '%')
                   OR u.email LIKE CONCAT('%', :q, '%'))
            ORDER BY a.nome ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<AthleteRow> findAthletes(
            @Param("trainerId") int trainerId,
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

        Integer getShirt_number();

        String getPosition();

        String getAvatar_url();
    }
}