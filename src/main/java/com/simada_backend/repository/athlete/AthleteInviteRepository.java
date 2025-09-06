package com.simada_backend.repository.athlete;

import com.simada_backend.model.athlete.invite.AthleteInvite;
import com.simada_backend.model.athlete.invite.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AthleteInviteRepository extends JpaRepository<AthleteInvite, Long> {
    Optional<AthleteInvite> findByToken(String token);
    Optional<AthleteInvite> findFirstByCoach_IdAndEmailAndStatus(Long coachId, String email, InviteStatus status);
    void deleteByEmail(String email);
}