package com.simada_backend.model.athlete;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "athlete_performance_snapshot")
@Getter
@Setter
public class AthletePerformanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;
    @Column(name = "coach_id", nullable = false)
    private Long coachId;
    @Column(name = "points", nullable = false)
    private Integer points;
    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;
}
