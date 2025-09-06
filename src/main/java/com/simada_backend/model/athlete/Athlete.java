package com.simada_backend.model.athlete;

import com.simada_backend.model.Coach;
import com.simada_backend.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "athlete")
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "position")
    private String position;
    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @OneToOne
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_coach", nullable = false)
    private Coach coach;

    @OneToOne(mappedBy = "athlete", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AthleteExtra extra;
}
