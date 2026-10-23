package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.config.ApiErrorExamples;
import com.academy.hamster_clinic.model.dto.ErrorResponse;
import com.academy.hamster_clinic.model.dto.OwnerRequest;
import com.academy.hamster_clinic.model.dto.OwnerResponse;
import com.academy.hamster_clinic.model.service.OwnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
@Tag(name = "Хозяева", description = "Операции с владельцами хомяков")
public class OwnerControler {
    
    private final OwnerService ownerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Создать хозяина", description = "Создаёт нового хозяина. Email должен быть уникальным.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Хозяин создан"),

            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации тела запроса",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validation failed", value = """
                                    {
                                      "timestamp": "2026-10-23T12:00:00+03:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Ошибка валидации",
                                      "path": "/api/owners",
                                      "fieldErrors": {
                                        "fullName": "ФИО обязательно",
                                        "email": "Некорректный email"
                                      }
                                    }
                                    """))),

            @ApiResponse(responseCode = "401",
                description = "Токен отсутствует, истёк или невалиден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                            @ExampleObject(name = "Unauthorized", value = ApiErrorExamples.UNAUTHORIZED),
                            @ExampleObject(name = "Token expired", value = ApiErrorExamples.TOKEN_EXPIRED),
                            @ExampleObject(name = "Invalid token", value = ApiErrorExamples.INVALID_TOKEN)
                        })),

            @ApiResponse(responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.FORBIDDEN))),

            @ApiResponse(responseCode = "409",
                    description = "Хозяин с таким email уже существует",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Duplicate email", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Хозяин с email уже существует: anna@example.com",
                                      "path": "/api/owners",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public OwnerResponse create(@RequestBody @Valid OwnerRequest req) {
        return ownerService.create(req);
    }

    // @GetMapping
    // @Operation(summary = "Вывести владельцев", description = "Выводит список всех существующих владельцев.")
    // public List<OwnerResponse> get() {
    //     return ownerService.list();
    // }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Вывести владельцев", description = "Можно с фильтрацией по ФИО")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список получен"),

            @ApiResponse(responseCode = "401",
                description = "Токен отсутствует, истёк или невалиден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                            @ExampleObject(name = "Unauthorized", value = ApiErrorExamples.UNAUTHORIZED),
                            @ExampleObject(name = "Token expired", value = ApiErrorExamples.TOKEN_EXPIRED),
                            @ExampleObject(name = "Invalid token", value = ApiErrorExamples.INVALID_TOKEN)
                        })),

            @ApiResponse(responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.FORBIDDEN))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public List<OwnerResponse> get(@Parameter(description = "ФИО Владельца", example = "Иванов Иван Иванович") @RequestParam (required = false) String fullName) {
        return ownerService.findByFullName(fullName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Получить хозяина по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Хозяин найден"),

            @ApiResponse(responseCode = "400",
                    description = "Неверный тип параметра id (ожидается число)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad id", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                      "path": "/api/owners/abc",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "401",
                description = "Токен отсутствует, истёк или невалиден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                            @ExampleObject(name = "Unauthorized", value = ApiErrorExamples.UNAUTHORIZED),
                            @ExampleObject(name = "Token expired", value = ApiErrorExamples.TOKEN_EXPIRED),
                            @ExampleObject(name = "Invalid token", value = ApiErrorExamples.INVALID_TOKEN)
                        })),

            @ApiResponse(responseCode = "403",
                    description = "Недостаточно прав (нужна роль ADMIN или MANAGER)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.FORBIDDEN))),

            @ApiResponse(responseCode = "404",
                    description = "Хозяин не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Owner not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Хозяин не найден: id=42",
                                      "path": "/api/owners/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public OwnerResponse get(@Parameter(description = "ID владельца") @PathVariable Long id) {
        return ownerService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Обновить данные о владельце", description = "Получает владельца по ID и обновляет его данные.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Хозяин обновлён"),

            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации тела или неверный тип id",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Ошибка валидации",
                                              "path": "/api/owners/1",
                                              "fieldErrors": {
                                                "fullName": "ФИО обязательно"
                                              }
                                            }
                                            """),
                                    @ExampleObject(name = "Bad id", value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                              "path": "/api/owners/abc",
                                              "fieldErrors": null
                                            }
                                            """)
                            })),

            @ApiResponse(responseCode = "401",
                description = "Токен отсутствует, истёк или невалиден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                            @ExampleObject(name = "Unauthorized", value = ApiErrorExamples.UNAUTHORIZED),
                            @ExampleObject(name = "Token expired", value = ApiErrorExamples.TOKEN_EXPIRED),
                            @ExampleObject(name = "Invalid token", value = ApiErrorExamples.INVALID_TOKEN)
                        })),

            @ApiResponse(responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.FORBIDDEN))),

            @ApiResponse(responseCode = "404",
                    description = "Хозяин не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Owner not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Хозяин не найден: id=42",
                                      "path": "/api/owners/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "409",
                    description = "Новый email уже занят другим хозяином",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Duplicate email", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Email уже занят: oleg@example.com",
                                      "path": "/api/owners/1",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public OwnerResponse update(@Parameter(description = "ID владельца") @PathVariable Long id, @RequestBody @Valid OwnerRequest req) {
        return ownerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Удалить владельца", description = "Получает владельца по ID и оудаляет его.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Хозяин удалён",
                    content = @Content),

            @ApiResponse(responseCode = "400",
                    description = "Неверный тип параметра id (ожидается число)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad id", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                      "path": "/api/owners/abc",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "401",
                description = "Токен отсутствует, истёк или невалиден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                            @ExampleObject(name = "Unauthorized", value = ApiErrorExamples.UNAUTHORIZED),
                            @ExampleObject(name = "Token expired", value = ApiErrorExamples.TOKEN_EXPIRED),
                            @ExampleObject(name = "Invalid token", value = ApiErrorExamples.INVALID_TOKEN)
                        })),

            @ApiResponse(responseCode = "403",
                    description = "Недостаточно прав",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.FORBIDDEN))),

            @ApiResponse(responseCode = "404",
                    description = "Хозяин не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Owner not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Хозяин не найден: id=42",
                                      "path": "/api/owners/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "409",
                    description = "У хозяина есть хомяки — удалить нельзя",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Owner has hamsters", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Нельзя удалить хозяина, у которого есть хомяки",
                                      "path": "/api/owners/1",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public void delete(@Parameter(description = "ID владельца") @PathVariable Long id) {
        ownerService.delete(id);
    }
    
}
