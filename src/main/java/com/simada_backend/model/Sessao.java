package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_termino")
    private LocalDateTime dataHoraTermino;

    @Column(name = "tipo_sessao", length = 45)
    private String tipoSessao;

    @Column(name = "descricao", length = 45)
    private String descricao;

    @Column(name = "local", length = 45)
    private String local;

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
