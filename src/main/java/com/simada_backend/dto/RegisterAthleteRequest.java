package com.simada_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAthleteRequest {
    private String email;
    private String password;
    private String fullName;
    private String gender;
    private String modality;
    private int shirtNumber;
    private String position;
}
