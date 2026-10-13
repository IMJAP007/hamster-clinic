package com.academy.hamster_clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DoctorRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Size(max = 100) String specialty,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email
) {}
