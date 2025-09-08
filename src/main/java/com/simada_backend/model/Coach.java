package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "coach")
@Getter @Setter
public class Coach {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "team")
    private String team;

    @OneToOne
    @JoinColumn(name = "id_user")
    private User user;
}
