package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "atleta")
public class Atleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atleta")
    private Long id_atleta;

    private String fullName;
    private String gender;
    private String modality;
    private int shirtNumber;
    private String position;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_treinador", nullable = false)
    private Treinador treinador;
}
