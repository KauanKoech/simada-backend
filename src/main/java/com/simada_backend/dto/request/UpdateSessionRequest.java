package com.simada_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;

public record UpdateSessionRequest(
        String type,
        String title,
        String date,
        String score,
        @JsonAlias({"description","notes"})
        String description,
        String location
) {}
