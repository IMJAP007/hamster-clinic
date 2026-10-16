package com.academy.hamster_clinic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiseaseRequest(
        @Schema(description = "Название диагноза", example = "Простуда")
        @NotBlank @Size(max = 200) String name
) {}
