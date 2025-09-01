package com.simada_backend.repository.athlete;

import com.simada_backend.model.AtletaExtra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AtletaExtraRepository extends JpaRepository<AtletaExtra, Long> {
    Optional<AtletaExtra> findByAtleta_IdAtleta(Long atletaId);
    boolean existsByAtleta_IdAtleta(Long atletaId);
}