package com.simada_backend.model.recommendation;

import com.simada_backend.model.Coach;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.session.Session;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "psy_recommendations",
        uniqueConstraints = @UniqueConstraint(name = "uq_psy_reco", columnNames = {"session_id", "athlete_id"})
)
public class PsyRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @Lob
    @Column(name = "text", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String text;

    @Column(name = "lang", length = 16)
    private String lang;

    @Column(name = "model", length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private Source source;

    public enum Source {groq, fallback}

    private Integer srpe;
    private Integer fatigue;
    private Integer soreness;
    private Integer mood;
    private Integer energy;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
