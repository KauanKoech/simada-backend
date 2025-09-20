package com.simada_backend.model.loadCalc;

import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.session.Session;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "session_loads")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SessionLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @Column(name = "load_srpe")
    private Double loadSrpe;

    @Column(name = "load_pl_sim")
    private Double loadPlSim;

    @Column(name = "load_effective")
    private Double loadEffective;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_source", nullable = false, length = 20)
    private LoadSource loadSource;

    @Column(name = "formula_version", nullable = false, length = 20)
    private String formulaVersion;

    @Column(name = "params_json", columnDefinition = "json")
    private String paramsJson;
}
