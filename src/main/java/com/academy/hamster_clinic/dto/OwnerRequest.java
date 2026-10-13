package com.academy.hamster_clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OwnerRequest(
    @NotBlank @Size(max = 200) String fullName,
    @Size(max = 30) String phone,
    @Email @Size(max = 150) String email
) {

}
