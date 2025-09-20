package com.simada_backend.model.session;

import com.simada_backend.model.Coach;
import com.simada_backend.model.athlete.Athlete;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "training_load_alert",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_alert_athlete_session",
                        columnNames = {"athlete_id", "session_id"}
                )
        }
)
public class TrainingLoadAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Athlete (N:1)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    // Coach (N:1)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    // Session (N:1)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

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

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP"
    )
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
