package com.academy.hamster_clinic.model.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record HamsterDiseaseRequest(
        @Schema(description = "ID хомяка", example = "1")
        @NotNull Long hamsterId,


        @Schema(description = "ID болзеи", example = "1")
        @NotNull Long diseaseId,

        @Schema(description = "Дата установки диагноза", example = "2026-10-16")
        @NotNull LocalDate diagnosedAt
) {}
