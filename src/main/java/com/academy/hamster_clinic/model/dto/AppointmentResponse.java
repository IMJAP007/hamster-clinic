package com.academy.hamster_clinic.model.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record AppointmentResponse(
        Long id,
        Long doctorId,
        String doctorFullName,
        String doctorSpecialty,
        Long ownerId,
        String ownerFullName,
        Long hamsterId,
        String hamsterName,
        LocalDateTime appointmentDate,
        OffsetDateTime createdAt
) {}
