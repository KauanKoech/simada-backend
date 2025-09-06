package com.simada_backend.repository.athlete;

import com.simada_backend.model.athlete.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    Optional<Athlete> findByIdAndCoach_Id(Long athleteId, Long coachId);

    // Busca exata por nome
    Optional<Athlete> findFirstByNameIgnoreCase(String name);

    // Busca por dorsal
    List<Athlete> findByJerseyNumber(Integer jerseyNumber);

   // Combinar nome + dorsal p/ ser mais específico
    Optional<Athlete> findFirstByNameIgnoreCaseAndJerseyNumber(String name, Integer jerseyNumber);

    @Query(
            value = """
                SELECT u.email
                FROM athlete a
                JOIN user u ON u.id_usuario = a.id_usuario
                WHERE a.id_atleta = :atletaId
                """,
            nativeQuery = true
    )
    Optional<String> findEmailByCoachId(@Param("coachId") Long athleteId);
}
