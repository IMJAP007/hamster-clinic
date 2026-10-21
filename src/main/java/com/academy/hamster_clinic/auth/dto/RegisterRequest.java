package com.academy.hamster_clinic.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя обязательно")
        @Size(max = 200)
        String fullName,

        @NotBlank(message = "Логин обязателен")
        @Email(message = "Логин должен быть email")
        @Size(max = 100)
        String username,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, max = 72, message = "Пароль должен быть от 8 до 72 символов")
        String password
) {}
