package com.academy.hamster_clinic.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(
        @NotNull Long doctorId,
        @NotNull Long ownerId,
        @NotNull Long hamsterId,
        @NotNull LocalDateTime appointmentDate
) {}
