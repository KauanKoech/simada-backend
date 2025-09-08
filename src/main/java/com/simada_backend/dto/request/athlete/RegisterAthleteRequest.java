package com.simada_backend.dto.request.athlete;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAthleteRequest {
    private int idCoach;
    private String email;
    private String password;
    private String name;
    private int jerseyNumber;
    private String position;
}
