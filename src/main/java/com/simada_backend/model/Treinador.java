package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "treinador")
public class Treinador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String modality;
    private String gender;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
