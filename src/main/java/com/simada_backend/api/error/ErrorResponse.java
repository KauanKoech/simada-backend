package com.simada_backend.api.error;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private List<FieldErrorItem> details;

    @Data
    @Builder
    public static class FieldErrorItem {
        private String field;
        private String message;
    }
}
