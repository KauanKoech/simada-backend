package com.simada_backend.repository.session;

import com.simada_backend.model.session.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Sessao, Integer> {}
