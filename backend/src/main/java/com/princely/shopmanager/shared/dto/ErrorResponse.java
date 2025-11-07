package com.princely.shopmanager.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    String code,
    String message,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, String> fieldErrors,
    LocalDateTime timestamp
) {
    public ErrorResponse(String code, String message) {
        this(code, message, null, LocalDateTime.now());
    }

    public ErrorResponse(String code, String message, Map<String, String> fieldErrors) {
        this(code, message, fieldErrors, LocalDateTime.now());
    }
}