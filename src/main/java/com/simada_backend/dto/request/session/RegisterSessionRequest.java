package com.simada_backend.dto.request.session;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterSessionRequest {

        @NotNull
        @JsonAlias({"coach_id", "coachId"})
        private Long coachId;

        @NotBlank
        private String type;

        @NotBlank
        private String title;

        @NotBlank
        @JsonAlias({"date", "data"})
        private String date;

        @JsonAlias({"athletes_count", "athletesCount"})
        private Integer athletesCount;

        private String score;

        @JsonAlias({"notes", "description"})
        private String notes;

        private String location;
}
