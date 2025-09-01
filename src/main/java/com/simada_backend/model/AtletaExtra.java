package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "atleta_extra")
public class AtletaExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_extra")
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atleta", unique = true, nullable = false)
    private Atleta atleta;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "lean_mass_kg", precision = 5, scale = 2)
    private BigDecimal leanMassKg;

    @Column(name = "fat_mass_kg", precision = 5, scale = 2)
    private BigDecimal fatMassKg;

    @Column(name = "body_fat_pct", precision = 5, scale = 2)
    private BigDecimal bodyFatPct;

    @Column(name = "dominant_foot", length = 10)
    private String dominantFoot; // Left | Right | Both

    @Column(name = "nationality", length = 80)
    private String nationality;

    @Column(name = "injury_status", length = 20)
    private String injuryStatus; // Healthy | Injured | Rehab
}
