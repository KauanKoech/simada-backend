package com.simada_backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterCoachRequest {
    private String email;
    private String password;
    private String name;
}
