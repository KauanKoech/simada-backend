package com.simada_backend.repository.session;

import com.simada_backend.model.Metricas;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MetricasRepository extends JpaRepository<Metricas, Integer> {

    @Modifying
    @Query("DELETE FROM Metricas m WHERE m.sessao.idSessao = :sessionId")
    int deleteBySessionId(@Param("sessionId") Integer sessionId);
}