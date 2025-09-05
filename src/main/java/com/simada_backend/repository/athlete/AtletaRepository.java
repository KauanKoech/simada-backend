package com.simada_backend.repository.athlete;

import com.simada_backend.model.athlete.Atleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtletaRepository extends JpaRepository<Atleta, Long> {
    Optional<Atleta> findByIdAtletaAndTreinador_Id(Long atletaId, Long treinadorId);

    // Busca exata por nome
    Optional<Atleta> findFirstByNomeIgnoreCase(String nome);

    // Busca por dorsal
    List<Atleta> findByNumeroCamisa(Integer numeroCamisa);

   // Combinar nome + dorsal p/ ser mais específico
    Optional<Atleta> findFirstByNomeIgnoreCaseAndNumeroCamisa(String nome, Integer numeroCamisa);

    @Query(
            value = """
                SELECT u.email
                FROM atleta a
                JOIN usuario u ON u.id_usuario = a.id_usuario
                WHERE a.id_atleta = :atletaId
                """,
            nativeQuery = true
    )
    Optional<String> findEmailByAtletaId(@Param("atletaId") Long atletaId);
}
