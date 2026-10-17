package com.academy.hamster_clinic.model.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {}
