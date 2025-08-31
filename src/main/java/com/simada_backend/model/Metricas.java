package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "metricas")
public class Metricas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metricas")
    private Integer idMetricas;

    // FK Sessao
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sessao", nullable = false)
    private Sessao sessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atleta")
    private Atleta atleta;

    @Column(name = "session")
    private String session;
    @Column(name = "task")
    private String task;
    @Column(name = "date")
    private LocalDate date;
    @Column(name = "position")
    private String position;
    @Column(name = "dorsal")
    private Integer dorsal;
    @Column(name = "player")
    private String player;

    @Column(name = "time", precision = 8, scale = 2)
    private BigDecimal time;

    @Column(name = "total_distance", precision = 10, scale = 2)
    private BigDecimal totalDistance;
    @Column(name = "minute_distance", precision = 10, scale = 2)
    private BigDecimal minuteDistance;

    @Column(name = "distance_vrange1", precision = 10, scale = 2)
    private BigDecimal distanceVrange1;
    @Column(name = "distance_vrange2", precision = 10, scale = 2)
    private BigDecimal distanceVrange2;
    @Column(name = "distance_vrange3", precision = 10, scale = 2)
    private BigDecimal distanceVrange3;
    @Column(name = "distance_vrange4", precision = 10, scale = 2)
    private BigDecimal distanceVrange4;
    @Column(name = "distance_vrange5", precision = 10, scale = 2)
    private BigDecimal distanceVrange5;
    @Column(name = "distance_vrange6", precision = 10, scale = 2)
    private BigDecimal distanceVrange6;

    @Column(name = "max_speed", precision = 6, scale = 2)
    private BigDecimal maxSpeed;
    @Column(name = "average_speed", precision = 6, scale = 2)
    private BigDecimal averageSpeed;

    @Column(name = "num_dec_expl")
    private Integer numDecExpl;
    @Column(name = "max_dec", precision = 6, scale = 2)
    private BigDecimal maxDec;
    @Column(name = "num_acc_expl")
    private Integer numAccExpl;
    @Column(name = "max_acc", precision = 6, scale = 2)
    private BigDecimal maxAcc;

    @Column(name = "player_load", precision = 10, scale = 2)
    private BigDecimal playerLoad;

    @Column(name = "hmld", precision = 10, scale = 2)
    private BigDecimal hmld;
    @Column(name = "hmld_count")
    private Integer hmldCount;
    @Column(name = "hmld_relative", precision = 10, scale = 2)
    private BigDecimal hmldRelative;
    @Column(name = "hmld_time", precision = 10, scale = 2)
    private BigDecimal hmldTime;

    @Column(name = "hid_intervals")
    private Integer hidIntervals;
    @Column(name = "num_hids")
    private Integer numHids;

    @Column(name = "hsr", precision = 10, scale = 2)
    private BigDecimal hsr;
    @Column(name = "sprints")
    private Integer sprints;
    @Column(name = "num_hsr")
    private Integer numHsr;

    @Column(name = "time_vrange4", precision = 10, scale = 2)
    private BigDecimal timeVrange4;

    @Column(name = "rpe", precision = 5, scale = 2)
    private BigDecimal rpe;
}