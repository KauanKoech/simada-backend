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

    @PrePersist
    private void prePersistSyncIdFromUser() {
        if (this.id == null && this.user != null) {
            this.id = this.user.getId();
        }
    }

    @PreUpdate
    private void preUpdateValidateIds() {
        if (this.user != null && this.id != null && !this.id.equals(this.user.getId())) {
            throw new IllegalStateException("Inconsistência: athlete.id (" + this.id + ") != user.id (" + this.user.getId() + ")");
        }
    }
}
