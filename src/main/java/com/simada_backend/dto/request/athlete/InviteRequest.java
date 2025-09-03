package com.simada_backend.dto.request.athlete;

import lombok.Data;

import java.time.LocalDate;

public class InviteRequest {
    public static class CreateInviteReq {
        public String email;
    }

    @Data
    public static class CompleteInviteReq {
        private String token;
        private String name;
        private String password;
        private String phone;
        private LocalDate birth;
        private String position;
    }
}