package com.simada_backend.repository.psychoForm;

import com.simada_backend.model.psychoForm.PsychoFormConvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PsychoFormConviteRepository extends JpaRepository<PsychoFormConvite, Long> {
    Optional<PsychoFormConvite> findByToken(String token);
}