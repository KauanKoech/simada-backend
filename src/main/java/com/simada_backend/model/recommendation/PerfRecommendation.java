package com.simada_backend.model.recommendation;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "perf_recommendations",
        uniqueConstraints = @UniqueConstraint(name = "uq_perf_reco", columnNames = {"session_id","athlete_id"}))
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PerfRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "athlete_id", nullable = false)
    private Long athleteId;

    @Column(name = "coach_id", nullable = false)
    private Long coachId;

    @Lob
    @Column(name = "text", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String text;

    @Column(name = "lang", length = 16)
    private String lang;

    @Column(name = "model", length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", columnDefinition = "ENUM('groq','fallback')")
    private Source source;

    public enum Source { groq, fallback }

    // Scores (auditoria)
    private Integer acwr;
    private Integer monotony;
    private Integer strain;
    private Integer pctQwUp;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;
}
