package com.simada_backend.repository.session;

import com.simada_backend.model.session.Metricas;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MetricasRepository extends JpaRepository<Metricas, Integer> {

    @Modifying
    @Query("DELETE FROM Metricas m WHERE m.sessao.idSessao = :sessionId")
    int deleteBySessionId(@Param("sessionId") Integer sessionId);

    @Query(
            value = "SELECT DISTINCT m.id_atleta FROM metricas m WHERE m.id_sessao = :sessionId",
            nativeQuery = true
    )
    List<Long> findAthletesBySessionId(@Param("sessionId") Long sessionId);
}