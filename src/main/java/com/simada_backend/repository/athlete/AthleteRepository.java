package com.simada_backend.repository.athlete;

import com.simada_backend.model.Coach;
import com.simada_backend.model.athlete.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    Optional<Athlete> findByIdAndCoach_Id(Long athleteId, Long coachId);

    @Override
    Optional<Athlete> findById(Long aLong);

    // Busca exata por nome
    Optional<Athlete> findFirstByNameIgnoreCase(String name);

    // Busca por dorsal
    List<Athlete> findByJerseyNumber(Integer jerseyNumber);

    // Combinar nome + dorsal p/ ser mais específico
    Optional<Athlete> findFirstByNameIgnoreCaseAndJerseyNumber(String name, Integer jerseyNumber);

    @Query("SELECT a.user.email FROM Athlete a WHERE a.id = :athleteId")
    Optional<String> findEmailByAthleteId(@Param("athleteId") Long athleteId);

    // principal: identificar atleta por coach + nome (case-insensitive)
    Optional<Athlete> findByCoach_IdAndNameIgnoreCase(Long coachId, String name);

    // opcional (se “dorsal” for confiável como chave)
    Optional<Athlete> findByCoach_IdAndJerseyNumber(Long coachId, Integer jerseyNumber);
}
