package com.simada_backend.model.session;

import com.simada_backend.model.Coach;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coach", nullable = false)
    private Coach coach;

    @Column(name = "coach_photo", length = 255)
    private String coach_Photo;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "session_type", length = 45)
    private String session_type;

    @Column(name = "title", length = 45)
    private String title;

    @Column(name = "score", length = 20)
    private String score;

    @Column(name = "description", length = 45)
    private String description;

    @Column(name = "local", length = 45)
    private String local;

    @Column(name = "num_athletes", length = 20)
    private Integer numAthletes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session s)) return false;
        return id != null && id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}