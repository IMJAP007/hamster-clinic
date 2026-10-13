package com.academy.hamster_clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HamsterRenameRequest(
        @NotBlank @Size(max = 100) String name
) {}
