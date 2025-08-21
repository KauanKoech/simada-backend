package com.simada_backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterTrainerRequest {
    private String email;
    private String password;
    private String fullName;
    private String modality;
    private String gender;
}
