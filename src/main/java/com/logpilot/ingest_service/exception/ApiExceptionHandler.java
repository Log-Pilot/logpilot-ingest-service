package com.logpilot.ingest_service.exception;

import com.logpilot.ingest_service.model.ApiErrorResponse;
import com.logpilot.ingest_service.model.IngestError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Pattern EVENT_FIELD = Pattern.compile("events\\[(\\d+)](?:\\.(.+))?");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<IngestError> errors = new ArrayList<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            Matcher matcher = EVENT_FIELD.matcher(fieldError.getField());
            if (matcher.matches()) {
                Integer index = Integer.valueOf(matcher.group(1));
                String field = matcher.group(2) == null ? "events" : toSnakeCase(matcher.group(2));
                errors.add(new IngestError(index, field, fieldError.getDefaultMessage()));
            } else {
                errors.add(new IngestError(null, toSnakeCase(fieldError.getField()), fieldError.getDefaultMessage()));
            }
        }
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse("invalid_request", "Request validation failed", errors)
        );
    }

    @ExceptionHandler(InvalidEventException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidEvents(InvalidEventException exception) {
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse("invalid_request", "Request validation failed", exception.getErrors())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody() {
        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(
                        "invalid_json",
                        "Request body is malformed or contains fields that cannot be parsed",
                        List.of(new IngestError(null, "body", "provide a valid ingestion request"))
                )
        );
    }

    @ExceptionHandler(RedisPublishException.class)
    public ResponseEntity<ApiErrorResponse> handleRedisUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new ApiErrorResponse(
                        "stream_unavailable",
                        "Events were not accepted because the stream is unavailable",
                        List.of()
                )
        );
    }

    private String toSnakeCase(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
