package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "convite_atleta",
        uniqueConstraints = @UniqueConstraint(name = "uk_treinador_email", columnNames = {"id_treinador", "email"})
)
@Getter
@Setter
@NoArgsConstructor
public class ConviteAtleta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_treinador")
    private Treinador trainer;

    @Column(nullable = false)
    private String email;
    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "aceito_em")
    private LocalDateTime acceptedAt;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
