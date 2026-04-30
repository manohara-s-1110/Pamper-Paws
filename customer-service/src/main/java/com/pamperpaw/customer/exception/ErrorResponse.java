package com.pamperpaw.customer.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private final String message;
    private final int status;
    private final String error;
    private final String path;
    private final LocalDateTime timestamp;
    private final Map<String, String> validationErrors;
}
