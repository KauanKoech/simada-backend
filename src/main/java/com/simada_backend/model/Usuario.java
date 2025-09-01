package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    private String nome;
    private String email;
    private String senha;
    private String foto;

    @Column(name = "tipo_usuario")
    private String tipoUsuario;

    @Column(name = "data_nascimento")
    private java.time.LocalDate dataNascimento;

    @Column(name = "telefone")
    private String telefone;
}
