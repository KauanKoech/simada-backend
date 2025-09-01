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
    private Long idAtleta;

    @Column(name = "nome")
    private String fullName;

    @Column(name = "sexo")
    private String gender;

    @Column(name = "modalidade")
    private String modality;

    @Column(name = "numero_camisa")
    private Integer shirtNumber;
    private String position;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_treinador", nullable = false)
    private Treinador treinador;

    @OneToOne(mappedBy = "atleta", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AtletaExtra extra;
}
