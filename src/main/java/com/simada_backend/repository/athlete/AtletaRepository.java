package com.simada_backend.repository.athlete;

import com.simada_backend.model.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtletaRepository extends JpaRepository<Atleta, Long> {
    Optional<Atleta> findByIdAtletaAndTreinador_Id(Long atletaId, Long treinadorId);

    // Busca exata por nome
    Optional<Atleta> findFirstByFullNameIgnoreCase(String fullName);

    // Busca por dorsal
    List<Atleta> findByShirtNumber(Integer shirtNumber);

   // Combinar nome + dorsal p/ ser mais específico
    Optional<Atleta> findFirstByFullNameIgnoreCaseAndShirtNumber(String fullName, Integer shirtNumber);
}
