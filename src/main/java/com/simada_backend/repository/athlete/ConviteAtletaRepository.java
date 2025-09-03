package com.simada_backend.repository.athlete;

import com.simada_backend.model.ConviteAtleta;
import com.simada_backend.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConviteAtletaRepository extends JpaRepository<ConviteAtleta, Long> {
    Optional<ConviteAtleta> findByToken(String token);
    Optional<ConviteAtleta> findFirstByTrainer_IdAndEmailAndStatus(Long trainerId, String email, InvitationStatus status);
}