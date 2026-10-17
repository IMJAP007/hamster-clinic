package com.academy.hamster_clinic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OwnerRequest(

    @Schema(description = "ФИО Врача", example = "Иванов Иван Иванович")
    @NotBlank @Size(max = 200) String fullName,
    
    @Schema(description = "Телефон", example = "7-999-999-99-99")
    @Size(max = 30) String phone,

    @Schema(description = "Почта", example = "email@example.com")
    @Email @Size(max = 150) String email
) {

}
