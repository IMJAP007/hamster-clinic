package com.academy.hamster_clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HamsterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Long ownerId
) {}
