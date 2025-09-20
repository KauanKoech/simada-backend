package com.simada_backend.api.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.time.OffsetDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, ServletWebRequest req) {
        return build(ex.getStatus(), ex.getCode().name(), ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, ServletWebRequest req) {
        List<ErrorResponse.FieldErrorItem> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ErrorResponse.FieldErrorItem.builder()
                        .field(fe.getField())
                        .message(resolveFieldMessage(fe))
                        .build())
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(),
                "One or more fields are invalids.", req, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintValidation(ConstraintViolationException ex, ServletWebRequest req) {
        List<ErrorResponse.FieldErrorItem> fields = ex.getConstraintViolations().stream()
                .map(this::toFieldItem)
                .toList();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(),
                "One or more parameters are invalid.", req, fields);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, ServletWebRequest req) {
        // MySQL duplicate key: error code 1062
        String msg = (ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "");
        if (msg != null && msg.contains("Duplicate") && msg.contains("email")) {
            return build(HttpStatus.CONFLICT, ErrorCode.EMAIL_IN_USE.name(),
                    "This email are already in use.", req, null);
        }
        return build(HttpStatus.CONFLICT, ErrorCode.CONSTRAINT_VIOLATION.name(),
                        "Unable to complete operation due to data conflict.", req, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, ServletWebRequest req) {
        HttpStatus status = ex.getStatusCode() instanceof HttpStatus http ? http : HttpStatus.BAD_REQUEST;
        String code = status == HttpStatus.UNAUTHORIZED ? ErrorCode.AUTH_INVALID_CREDENTIALS.name()
                : ErrorCode.INTERNAL_ERROR.name();
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, code, message, req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, ServletWebRequest req) {
        ErrorResponse.FieldErrorItem item = ErrorResponse.FieldErrorItem.builder()
                .field(ex.getParameterName())
                .message("Missing mandatory parameter.")
                .build();
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.name(),
                "Missing or invalid parameters.", req, List.of(item));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex, ServletWebRequest req) {
        // Logar ex detalhadamente no servidor
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected error occurred. Please try again.", req, null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                ServletWebRequest req, List<ErrorResponse.FieldErrorItem> details) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(req.getRequest().getRequestURI())
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    private String resolveFieldMessage(FieldError fe) {
        return fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value.";
    }

    private ErrorResponse.FieldErrorItem toFieldItem(ConstraintViolation<?> cv) {
        String field = cv.getPropertyPath() != null ? cv.getPropertyPath().toString() : null;
        return ErrorResponse.FieldErrorItem.builder()
                .field(field)
                .message(cv.getMessage())
                .build();
    }
}