package io.reactive.reservation.adapter.web.errorhandling;

import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Collection;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public record ApiErrorResponse(Instant timestamp,
                               Integer status,
                               HttpStatus errorCode,
                               String message,
                               String debugMessage,
                               Collection<ApiErrorField> errors) {

    @Builder
    public ApiErrorResponse(HttpStatus httpStatus, String message, String debugMessage, Collection<ApiErrorField> errors) {
        this(Instant.now(), httpStatus.value(), httpStatus, message, debugMessage, errors);
    }

    public static ResponseEntity<ApiErrorResponse> notFound(String message) {
        return ResponseEntity.status(NOT_FOUND)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(NOT_FOUND)
                        .message(message)
                        .build());
    }

    public static ResponseEntity<ApiErrorResponse> conflict(String message) {
        return ResponseEntity.status(CONFLICT)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(CONFLICT)
                        .message(message)
                        .build());

    }

    public record ApiErrorField(String field, String message, Object rejectedValue) {
    }
}
