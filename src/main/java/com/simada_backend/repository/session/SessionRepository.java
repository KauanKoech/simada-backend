package com.simada_backend.repository.session;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {}
