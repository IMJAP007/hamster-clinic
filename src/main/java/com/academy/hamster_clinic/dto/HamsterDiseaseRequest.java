package com.academy.hamster_clinic.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record HamsterDiseaseRequest(
        @NotNull Long hamsterId,
        @NotNull Long diseaseId,
        @NotNull LocalDate diagnosedAt
) {}
