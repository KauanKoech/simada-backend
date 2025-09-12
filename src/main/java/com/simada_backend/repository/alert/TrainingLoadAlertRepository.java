package com.simada_backend.repository.alert;

import com.simada_backend.dto.response.alert.PerformanceAlertDTO;
import com.simada_backend.dto.response.alert.PerformanceAnswerDTO;
import com.simada_backend.model.session.TrainingLoadAlert;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingLoadAlertRepository extends CrudRepository<TrainingLoadAlert, Long> {

    @Query("""
            SELECT new com.simada_backend.dto.response.alert.PerformanceAlertDTO(
                t.id, 
                t.athleteId, 
                t.coachId,
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
            JOIN Athlete a ON a.id = t.athleteId
            JOIN User u ON u.id = a.user.id
            WHERE t.coachId = :coachId
            ORDER BY t.createdAt DESC
            """)
    List<PerformanceAlertDTO> findByCoachId(Long coachId);

    @Query("""
            SELECT new com.simada_backend.dto.response.alert.PerformanceAnswerDTO(
                t.id, 
                t.athleteId, 
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
            JOIN Athlete a ON a.id = t.athleteId
            JOIN AthleteExtra ax ON ax.athlete.id = t.athleteId
            JOIN User u ON u.id = a.user.id
            WHERE t.athleteId = :athleteId                         
            """)
    Optional<PerformanceAnswerDTO> findAnswerBySessionAndAthlete(
            @Param("athleteId") Long athleteId
    );

    Optional<TrainingLoadAlert> findByAthleteIdAndSessionId(Long athleteId, Long sessionId);
}
