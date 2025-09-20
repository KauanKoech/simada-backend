package com.simada_backend.model.psycho;

import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.session.Session;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "psycho_form_answer")
public class PsychoFormAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_athlete", unique = true, nullable = false)
    private Athlete athlete;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_session", nullable = false)
    private Session idSession;

    @Column(name = "srpe", nullable = false)
    private int srpe;

    @Column(name = "fatigue", nullable = false)
    private int fatigue;

    @Column(name = "soreness", nullable = false)
    private int soreness;

    @Column(name = "mood", nullable = false)
    private int mood;

    @Column(name = "energy", nullable = false)
    private int energy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

}