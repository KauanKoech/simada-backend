package com.simada_backend.model.session;


import com.simada_backend.model.athlete.Athlete;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "metrics")
public class Metrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // FK Sessao
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_session", nullable = false)
    private Session session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_athlete")
    private Athlete athlete;

    @Column(name = "session")
    private String sessionName;
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

    @Column(name = "time")
    private Double time;

    @Column(name = "total_distance")
    private Double totalDistance;
    @Column(name = "minute_distance")
    private Double minuteDistance;

    @Column(name = "distance_vrange1")
    private Double distanceVrange1;
    @Column(name = "distance_vrange2")
    private Double distanceVrange2;
    @Column(name = "distance_vrange3")
    private Double distanceVrange3;
    @Column(name = "distance_vrange4")
    private Double distanceVrange4;
    @Column(name = "distance_vrange5")
    private Double distanceVrange5;
    @Column(name = "distance_vrange6")
    private Double distanceVrange6;

    @Column(name = "max_speed")
    private Double maxSpeed;
    @Column(name = "average_speed")
    private Double averageSpeed;

    @Column(name = "num_dec_expl")
    private Integer numDecExpl;
    @Column(name = "max_dec")
    private Double maxDec;
    @Column(name = "num_acc_expl")
    private Integer numAccExpl;
    @Column(name = "max_acc")
    private Double maxAcc;

    @Column(name = "player_load")
    private Double playerLoad;

    @Column(name = "hmld")
    private Double hmld;
    @Column(name = "hmld_count")
    private Integer hmldCount;
    @Column(name = "hmld_relative")
    private Double hmldRelative;
    @Column(name = "hmld_time")
    private Double hmldTime;

    @Column(name = "hid_intervals")
    private Integer hidIntervals;
    @Column(name = "num_hids")
    private Integer numHids;

    @Column(name = "hsr")
    private Double hsr;
    @Column(name = "sprints")
    private Integer sprints;
    @Column(name = "num_hsr")
    private Integer numHsr;

    @Column(name = "time_vrange4")
    private Double timeVrange4;

    @Column(name = "rpe")
    private Double rpe;
}