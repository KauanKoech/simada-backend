package com.simada_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "photo")
    private String photo;

    @Column(name = "gender")
    private String gender;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "birthDate")
    private java.time.LocalDate birthDate;

    @Column(name = "phone")
    private String phone;
}
