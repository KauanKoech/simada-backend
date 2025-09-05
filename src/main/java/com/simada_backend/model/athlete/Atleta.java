package com.simada_backend.model.athlete;

import com.simada_backend.model.Treinador;
import com.simada_backend.model.Usuario;
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
    private String nome;
    @Column(name = "sexo")
    private String sexo;
    @Column(name = "modalidade")
    private String modalidade;
    @Column(name = "posicao")
    private String posicao;
    @Column(name = "numero_camisa")
    private Integer numeroCamisa;

    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_treinador", nullable = false)
    private Treinador treinador;

    @OneToOne(mappedBy = "atleta", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AtletaExtra extra;
}
