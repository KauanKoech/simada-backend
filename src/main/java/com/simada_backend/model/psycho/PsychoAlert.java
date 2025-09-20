package com.simada_backend.model.psycho;

import com.simada_backend.model.Coach;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.session.Session;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "psycho_alert",
        indexes = {
                @Index(name = "idx_alert_coach", columnList = "coach_id"),
                @Index(name = "idx_alert_coach_created", columnList = "coach_id, created_at"),
                @Index(name = "idx_alert_session", columnList = "session_id"),
                @Index(name = "idx_alert_athlete", columnList = "athlete_id"),
                @Index(name = "idx_alert_answer", columnList = "answer_id")
        }
)
public class PsychoAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    // Session (N:1)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private PsychoFormAnswer answer;

    @Column(name = "rule_code", nullable = false, length = 64)
    private String ruleCode;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
