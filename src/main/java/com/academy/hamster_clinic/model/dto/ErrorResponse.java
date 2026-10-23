package com.academy.hamster_clinic.model.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Единый формат ответа об ошибке")
public record ErrorResponse(
        
        @Schema(description = "Время ошибки", example = "2026-10-23T12:00:00+03:00")
        OffsetDateTime timestamp,

        @Schema(description = "HTTP-код", example = "404")
        int status,

        @Schema(description = "Краткое название", example = "Not Found")
        String error,

        @Schema(description = "Человекочитаемое описание", example = "Хозяин не найден: id=42")
        String message,

        @Schema(description = "Путь запроса", example = "/api/owners/42")
        String path,

        @Schema(description = "Ошибки валидации по полям (только для 400)", nullable = true, example = "{\"fullName\": \"Имя обязательно\"}")
        Map<String, String> fieldErrors
) {}
