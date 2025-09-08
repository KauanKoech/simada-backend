package com.simada_backend.model.psycho;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "psycho_risk_score")
public class PsychoRiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coach_id", nullable = false)
    private Long coachId;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "answer_id", nullable = false, unique = true)
    private Long answerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 16)
    private RiskLevel riskLevel;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}