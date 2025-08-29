package com.simada_backend.repository.session;

import com.simada_backend.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Sessao, Integer> {}
