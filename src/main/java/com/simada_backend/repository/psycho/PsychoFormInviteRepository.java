package com.simada_backend.repository.psycho;

import com.simada_backend.model.psychoForm.PsychoFormInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsychoFormInviteRepository extends JpaRepository<PsychoFormInvite, Long> {
    Optional<PsychoFormInvite> findByToken(String token);
}