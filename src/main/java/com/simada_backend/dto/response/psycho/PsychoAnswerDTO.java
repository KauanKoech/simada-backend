package com.simada_backend.dto.response.psycho;

import java.time.LocalDate;

public record PsychoAnswerDTO(
        Long id,
        Long id_session,
        Long id_athlete,
        String token,
        LocalDate submitted_at,
        Integer srpe,
        Integer fatigue,
        Integer soreness,
        Integer mood,
        Integer energy,

        //Athlete Info
        String athlete_name,
        String athlete_email,
        String athlete_photo,
        String athlete_position
) {}
