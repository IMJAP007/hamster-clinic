package com.academy.hamster_clinic.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.academy.hamster_clinic.config.ApiErrorExamples;
import com.academy.hamster_clinic.model.dto.ErrorResponse;
import com.academy.hamster_clinic.security.auth.dto.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и логин")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация нового пользователя",
               description = "Создаёт пользователя с ролью ROLE_DOCTOR по умолчанию и сразу выдаёт JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь создан, токены выданы"),

            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации тела (пустые поля, неверный email, короткий пароль)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validation failed", value = """
                                    {
                                      "timestamp": "2026-10-24T12:00:00+03:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Ошибка валидации",
                                      "path": "/api/auth/register",
                                      "fieldErrors": {
                                        "fullName": "Имя обязательно",
                                        "username": "Логин должен быть email",
                                        "password": "Пароль должен быть от 8 до 72 символов"
                                      }
                                    }
                                    """))),

            @ApiResponse(responseCode = "409",
                    description = "Пользователь с таким логином (email) уже существует",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Username taken", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Пользователь с таким логином уже существует",
                                      "path": "/api/auth/register",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public AuthResponse register(@RequestBody @Valid RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Логин", description = "Принимает логин и пароль, возвращает JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход, токены выданы"),

            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации тела (пустой логин или пароль)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validation failed", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Ошибка валидации",
                                      "path": "/api/auth/login",
                                      "fieldErrors": {
                                        "username": "must not be blank",
                                        "password": "must not be blank"
                                      }
                                    }
                                    """))),

            @ApiResponse(responseCode = "401",
                    description = "Неверный логин или пароль (или учётная запись отключена)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad credentials", value = """
                                    {
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Неверный логин или пароль",
                                      "path": "/api/auth/login",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить access-токен")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Новая пара токенов выдана, старый refresh отозван"),

            @ApiResponse(responseCode = "400",
                    description = "Пустой refresh-токен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validation failed", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Ошибка валидации",
                                      "path": "/api/auth/refresh",
                                      "fieldErrors": {
                                        "refreshToken": "Refresh-токен обязателен"
                                      }
                                    }
                                    """))),

            @ApiResponse(responseCode = "409",
                    description = "Refresh-токен недействителен, истёк, отозван или уже был использован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Invalid token", value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Недействительный refresh-токен",
                                              "path": "/api/auth/refresh",
                                              "fieldErrors": null
                                            }
                                            """),
                                    @ExampleObject(name = "Expired token", value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Срок действия refresh-токена истёк",
                                              "path": "/api/auth/refresh",
                                              "fieldErrors": null
                                            }
                                            """),
                                    @ExampleObject(name = "Token reuse", value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Обнаружено повторное использование токена. Все сессии отозваны.",
                                              "path": "/api/auth/refresh",
                                              "fieldErrors": null
                                            }
                                            """)
                            })),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }
}
