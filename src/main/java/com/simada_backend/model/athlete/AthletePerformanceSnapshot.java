package com.simada_backend.model.athlete;

import com.simada_backend.model.Coach;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "athlete_performance_snapshot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthletePerformanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", unique = true, nullable = false)
    private Athlete athlete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @Column(name = "points", nullable = false)
    private Integer points;
    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;
}
