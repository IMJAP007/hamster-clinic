package com.academy.hamster_clinic.dto;

import java.time.OffsetDateTime;

public record DoctorResponse(
        Long id,
        String fullName,
        String specialty,
        String phone,
        String email,
        OffsetDateTime createdAt
) {}