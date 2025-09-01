package com.simada_backend.dto.request.athlete;

import com.simada_backend.dto.response.athlete.AthleteExtraDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAthleteRequest {
    private String name;
    private String email;
    private String phone;
    private String birth;
    private String position;
    private AthleteExtraDTO extra;
}