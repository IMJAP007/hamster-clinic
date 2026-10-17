package com.academy.hamster_clinic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HamsterRenameRequest(

        @Schema(description = "Имя", example = "Пушок")
        @NotBlank @Size(max = 100) String name
) {}
