package com.simada_backend.service.loadCalculator;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "session_loads")
public class SessionLoad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;

    @Column(name = "load_srpe")
    private Double loadSrpe;

    @Column(name = "load_pl_sim")
    private Double loadPlSim;

    @Column(name = "load_effective")
    private Double loadEffective;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_source", nullable = false)
    private LoadSource loadSource;

    @Column(name = "formula_version", nullable = false, length = 20)
    private String formulaVersion;

    @Column(name = "params_json", columnDefinition = "json")
    private String paramsJson;

    public SessionLoad() {
    }
}

