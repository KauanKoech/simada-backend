package com.simada_backend.repository.psycho;

import com.simada_backend.model.psycho.PsychoFormInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PsychoFormInviteRepository extends JpaRepository<PsychoFormInvite, Long> {
    Optional<PsychoFormInvite> findByToken(String token);

    List<PsychoFormInvite> findAllByIdSession_IdAndStatus(Long sessionId, String status);

    long countByIdSession_IdAndStatus(Long sessionId, String status);

    Optional<PsychoFormInvite> findFirstByIdCoach_IdAndEmailAndStatus(Long coachId, String email, String status);
}