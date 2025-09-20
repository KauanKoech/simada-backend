package com.simada_backend.model.psycho;

import com.simada_backend.model.Coach;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.session.Session;
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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coach", nullable = false)
    private Coach idCoach;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_athlete", unique = true, nullable = false)
    private Athlete idAthlete;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_session", nullable = false)
    private Session idSession;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}