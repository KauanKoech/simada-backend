package com.simada_backend.model.psycho;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "psycho_form_invite")
public class PsychoFormInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "id_coach", nullable = false)
    private Long idCoach;

    @Column(name = "id_athlete", nullable = false)
    private Long idAthlete;

    @Column(name = "id_session", nullable = false)
    private Long idSession;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}