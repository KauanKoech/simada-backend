package com.simada_backend.model.psychoForm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "psico_form_convite")
public class PsychoFormConvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "id_treinador", nullable = false)
    private Long idTreinador;

    @Column(name = "id_atleta", nullable = false)
    private Long idAtleta;

    @Column(name = "id_sessao", nullable = false)
    private Long idSessao;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}