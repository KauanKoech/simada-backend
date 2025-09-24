package com.simada_backend.repository.alert;

import com.simada_backend.dto.response.alert.PerformanceAlertDTO;
import com.simada_backend.dto.response.alert.PerformanceAnswerDTO;
import com.simada_backend.model.session.TrainingLoadAlert;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingLoadAlertRepository extends CrudRepository<TrainingLoadAlert, Long> {

    Optional<TrainingLoadAlert> findFirstByAthlete_IdOrderByCreatedAtDesc(Long athleteId);

    @Query("""
            SELECT new com.simada_backend.dto.response.alert.PerformanceAlertDTO(
                t.id, 
                t.athlete.id, 
                t.coach.id,
                t.acwr, 
                t.monotony, 
                t.strain, 
                t.pctQwUp,
                t.acwrLabel, 
                t.monotonyLabel, 
                t.strainLabel, 
                t.pctQwUpLabel,
                CAST(t.createdAt AS string),
                CAST(t.qwStart AS string),
                a.name,
                u.photo
            )
            FROM TrainingLoadAlert t
            JOIN Athlete a ON a.id = t.athlete.id
            JOIN User u ON u.id = a.user.id
            WHERE t.coach.id = :coachId
            ORDER BY t.createdAt DESC
            """)
    List<PerformanceAlertDTO> findByCoachId(Long coachId);

    Optional<TrainingLoadAlert> findByIdAndCoach_Id(Long id, Long coachId);

    @Query("""
            SELECT new com.simada_backend.dto.response.alert.PerformanceAnswerDTO(
                t.id, 
                t.athlete.id, 
                a.name,
                u.email,
                a.position,
                u.photo,
                ax.nationality,
                CAST(t.qwStart AS string),
                CAST(t.createdAt AS string),
                t.acwr, 
                t.monotony, 
                t.strain, 
                t.pctQwUp,
                t.acwrLabel, 
                t.monotonyLabel, 
                t.strainLabel, 
                t.pctQwUpLabel
            )
            FROM TrainingLoadAlert t
            JOIN Athlete a ON a.id = t.athlete.id
            JOIN AthleteExtra ax ON ax.athlete.id = t.athlete.id
            JOIN User u ON u.id = a.user.id
            WHERE t.athlete.id = :athleteId                         
            """)
    Optional<PerformanceAnswerDTO> findAnswerBySessionAndAthlete(
            @Param("athleteId") Long athleteId
    );

    Optional<TrainingLoadAlert> findByAthleteIdAndSessionId(Long athleteId, Long sessionId);

    @Modifying
    @Query("delete from TrainingLoadAlert a where a.athlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);
}
