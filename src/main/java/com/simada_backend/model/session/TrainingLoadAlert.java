package com.simada_backend.model.session;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Instant;

@Entity
@Table(name = "training_load_alert",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_alert_athlete_session", columnNames = {"athlete_id", "session_id"})
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingLoadAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;

    @Column(name = "coach_id", nullable = false)
    private Long coachId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "qw_start", nullable = false)
    private LocalDate qwStart;

    @Column(name = "acwr")
    private Double acwr;
    @Column(name = "acwr_label", length = 20)
    private String acwrLabel;

    @Column(name = "pct_qw_up")
    private Double pctQwUp;
    @Column(name = "pct_qw_up_label", length = 20)
    private String pctQwUpLabel;

    @Column(name = "monotony")
    private Double monotony;
    @Column(name = "monotony_label", length = 20)
    private String monotonyLabel;

    @Column(name = "strain")
    private Double strain;
    @Column(name = "strain_label", length = 20)
    private String strainLabel;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;
}