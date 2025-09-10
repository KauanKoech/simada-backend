package com.simada_backend.repository.athlete;

import com.simada_backend.model.athlete.Athlete;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AthleteProfileRepository extends JpaRepository<Athlete, Long> {

    @Query(value = """
            SELECT
                a.id                               AS id,
                COALESCE(a.name, u.name)           AS name,
                u.email                            AS email,
                u.gender                           AS gender,
                u.phone                            AS phone,
                ax.nationality                     AS nationality,
                u.photo                            AS photoUrl
            FROM athlete a
            LEFT JOIN `user` u   ON u.id = a.id_user
            LEFT JOIN athlete_extra ax ON ax.id_athlete = a.id
            WHERE a.id = :athleteId
            """, nativeQuery = true)
    Optional<ProfileProjection> findProfile(@Param("athleteId") Long athleteId);

    interface ProfileProjection {
        Long getId();

        String getName();

        String getEmail();

        String getGender();

        String getPhone();

        String getNationality();

        String getPhotoUrl();
    }

    /**
     * Atualiza nome “esportivo” do athlete (opcional)
     */
    @Modifying
    @Query(value = "UPDATE athlete SET name = :name WHERE id = :athleteId", nativeQuery = true)
    int updateAthleteName(@Param("athleteId") Long athleteId, @Param("name") String name);

    /**
     * Atualiza campos do user
     */
    @Modifying
    @Query(value = """
            UPDATE `user`
            SET name = COALESCE(:name, name),
                email = COALESCE(:email, email),
                gender = COALESCE(:gender, gender),
                phone  = COALESCE(:phone,  phone)
            WHERE id = :userId
            """, nativeQuery = true)
    int updateUserBasics(@Param("userId") Long userId,
                         @Param("name") String name,
                         @Param("email") String email,
                         @Param("gender") String gender,
                         @Param("phone") String phone);

    /**
     * Upsert de nationality em athlete_extra
     */
    @Modifying
    @Query(value = """
            INSERT INTO athlete_extra (id_athlete, nationality)
            VALUES (:athleteId, :nationality)
            ON DUPLICATE KEY UPDATE nationality = VALUES(nationality)
            """, nativeQuery = true)
    int upsertNationality(@Param("athleteId") Long athleteId,
                          @Param("nationality") String nationality);

    /**
     * Atualiza URL da foto do user
     */
    @Modifying
    @Query(value = "UPDATE `user` SET photo = :photoUrl WHERE id = :userId", nativeQuery = true)
    int updateUserPhoto(@Param("userId") Long userId, @Param("photoUrl") String photoUrl);

    /**
     * Lê hash da senha atual
     */
    @Query(value = "SELECT password FROM `user` WHERE id = :userId", nativeQuery = true)
    String getPasswordHash(@Param("userId") Long userId);

    /**
     * Atualiza senha (hash)
     */
    @Modifying
    @Query(value = "UPDATE `user` SET password = :hash WHERE id = :userId", nativeQuery = true)
    int setPasswordHash(@Param("userId") Long userId, @Param("hash") String hash);

    /**
     * Resolve userId a partir do athleteId
     */
    @Query(value = "SELECT id_user FROM athlete WHERE id = :athleteId", nativeQuery = true)
    Long findUserIdByAthleteId(@Param("athleteId") Long athleteId);
}

