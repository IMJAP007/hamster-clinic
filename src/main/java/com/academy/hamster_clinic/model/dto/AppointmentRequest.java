package com.academy.hamster_clinic.model.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(
        @Schema(description = "ID врача", example = "1")
        @NotNull Long doctorId,
        
        @Schema(description = "ID владельца", example = "1")
        @NotNull Long ownerId,
        

        @Schema(description = "ID хомяка", example = "1")
        @NotNull Long hamsterId,

        @Schema(description = "Дата записи", example = "2026-10-16T10:30:00")
        @NotNull LocalDateTime appointmentDate
) {}
