package com.simada_backend.model.athlete.invite;

import com.simada_backend.model.Coach;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "athlete_invite",
        uniqueConstraints = @UniqueConstraint(name = "uk_coach_email", columnNames = {"id_coach", "email"})
)
@Getter
@Setter
@NoArgsConstructor
public class AthleteInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_coach")
    private Coach coach;

    @Column(nullable = false, name = "email")
    private String email;

    @Column(nullable = false, unique = true, name = "token")
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
