package com.example.shop.common.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY) // Hide field "errors", if map is empty
public record ErrorResponse(
        int status,
        String message,
        Instant timestamp,
        Map<String, String> errors
) {
    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(status.value(), message, Instant.now(), Map.of());
    }

    public static ErrorResponse ofValidation(String message, Map<String, String> errors) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, Instant.now(), errors);
    }
}