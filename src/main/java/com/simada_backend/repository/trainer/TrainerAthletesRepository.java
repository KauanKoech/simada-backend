package com.simada_backend.repository.trainer;

import com.simada_backend.model.Atleta;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainerAthletesRepository extends JpaRepository<Atleta, Integer> {

    @Query(value = """
            SELECT
              a.id_atleta                         AS id,
              a.nome                              AS name,
              u.email                             AS email,
              a.data_nascimento                   AS birth,
              a.telefone                          AS phone,
              u.foto                              AS avatar_url,
              CASE a.status
                WHEN 'ATIVO'     THEN 'active'
                WHEN 'LESIONADO' THEN 'injured'
                WHEN 'INATIVO'   THEN 'inactive'
                ELSE NULL
              END                                 AS status
            FROM atleta a
            JOIN usuario u ON u.id_usuario = a.id_usuario
            WHERE a.id_treinador = :trainerId
              AND (:q IS NULL OR :q = '' OR a.nome LIKE CONCAT('%', :q, '%') OR u.email LIKE CONCAT('%', :q, '%'))
              AND (:status IS NULL OR :status = '' OR
                   CASE a.status
                     WHEN 'ATIVO'     THEN 'active'
                     WHEN 'LESIONADO' THEN 'injured'
                     WHEN 'INATIVO'   THEN 'inactive'
                     ELSE NULL
                   END = :status)
            ORDER BY a.nome ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<AthleteRow> findAthletes(
            @Param("trainerId") int trainerId,
            @Param("q") String q,
            @Param("status") String status,   // "active" | "injured" | "inactive"
            @Param("limit") int limit,
            @Param("offset") int offset);

    interface AthleteRow {
        Long getId();
        String getName();
        String getEmail();
        LocalDate getBirth();
        String getPhone();
        String getAvatar_url();
        String getStatus();
    }
}
