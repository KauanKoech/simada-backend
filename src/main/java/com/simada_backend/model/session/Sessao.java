package com.simada_backend.model.session;

import com.simada_backend.model.Treinador;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sessao")
public class Sessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sessao")
    private Integer idSessao;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_treinador", nullable = false)
    private Treinador treinador;

    @Column(name = "foto_treinador", length = 255)
    private String fotoTreinador;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "tipo_sessao", length = 45)
    private String tipoSessao;

    @Column(name = "titulo", length = 45)
    private String titulo;

    @Column(name = "placar", length = 20)
    private String placar;

    @Column(name = "descricao", length = 45)
    private String descricao;

    @Column(name = "local", length = 45)
    private String local;

    @Column(name = "num_atletas", length = 20)
    private Integer numAtletas;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sessao s)) return false;
        return idSessao != null && idSessao.equals(s.idSessao);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
