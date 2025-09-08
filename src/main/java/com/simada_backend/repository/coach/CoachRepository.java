package com.simada_backend.repository.coach;

import com.simada_backend.model.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Long> {
    @Query(value = """
              SELECT c.* FROM coach c
              JOIN `user` u ON u.id = c.id_user
              WHERE c.id = :id
            """, nativeQuery = true)
    Optional<Coach> findByIdWithUser(@Param("id") Long id);
}
