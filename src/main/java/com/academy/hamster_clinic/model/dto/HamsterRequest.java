package com.academy.hamster_clinic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record HamsterRequest(

        @Schema(description = "Имя", example = "Пушок")
        @NotBlank @Size(max = 100) String name,

        @Schema(description = "ID владельца", example = "1")
        @NotNull Long ownerId,

        @Size(max = 2048)
        @Pattern(
                regexp = "^(https?://).+\\.(jpg|jpeg|png|gif|webp|bmp|svg)(\\?.*)?$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Ссылка должна вести к изображению (jpg, png, gif, webp, bmp, svg)")
        @Schema(description = "Ссылка на изображение (jpg, png, gif, webp, bmp, svg)",
                example = "https://example.com/hamsters/rijik.jpg")
        String imageUrl
) {}
