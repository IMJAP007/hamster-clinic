package com.academy.hamster_clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DiseaseRequest(
        @NotBlank @Size(max = 200) String name
) {}
