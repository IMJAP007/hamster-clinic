package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.config.ApiErrorExamples;
import com.academy.hamster_clinic.model.dto.ErrorResponse;
import com.academy.hamster_clinic.model.dto.HamsterRenameRequest;
import com.academy.hamster_clinic.model.dto.HamsterRequest;
import com.academy.hamster_clinic.model.dto.HamsterResponse;
import com.academy.hamster_clinic.model.service.HamsterService;

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
@RequestMapping("/api/hamsters")
@RequiredArgsConstructor
@Tag(name = "Хомяки", description = "Операции с хомяками")
public class HamsterController {

    private final HamsterService hamsterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Хомяк создан"),

            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации тела (пустое имя, слишком длинный URL и т.п.)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validation failed", value = """
                                    {
                                      "timestamp": "2026-10-23T12:00:00+03:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Ошибка валидации",
                                      "path": "/api/hamsters",
                                      "fieldErrors": {
                                        "name": "Имя обязательно",
                                        "ownerId": "must not be null"
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

            @ApiResponse(responseCode = "404",
                    description = "Хозяин с указанным ownerId не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Owner not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Хозяин не найден: id=99",
                                      "path": "/api/hamsters",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "409",
                    description = "Ссылка imageUrl не ведёт к изображению (или другой бизнес-конфликт)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Not an image", value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Ссылка не ведёт к изображению (Content-Type: text/html)",
                                              "path": "/api/hamsters",
                                              "fieldErrors": null
                                            }
                                            """),
                                    @ExampleObject(name = "Private address", value = """
                                            {
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Ссылки на локальные и приватные адреса запрещены",
                                              "path": "/api/hamsters",
                                              "fieldErrors": null
                                            }
                                            """)
                            })),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    @Operation(summary = "Создать хомяка", description = "Создаёт нового хомяка. Нужен хозяин.")
    public HamsterResponse create(@RequestBody @Valid HamsterRequest req) {
        return hamsterService.create(req);
    }

    // @GetMapping
    // @Operation(summary = "Вывести список хомяков", description = "Можно с фильтрацией по владельцу")
    // public List<HamsterResponse> list(@Parameter(description = "ID владельца") @RequestParam(required = false) Long ownerId) {
    //     return hamsterService.list(ownerId);
    // }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Вывести список хомяков", description = "Можно с фильтрацией по ФИО владельца")
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
    public List<HamsterResponse> list(@Parameter(description = "ФИО владельца", example = "Иванов Иван Иванович") @RequestParam(required = false) String ownerFullName) {
        return hamsterService.list(ownerFullName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Вывести хомяка", description = "По ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Хомяк найден"),

            @ApiResponse(responseCode = "400",
                    description = "Неверный тип параметра id (ожидается число)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad id", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                      "path": "/api/hamsters/abc",
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
                    description = "Хомяк не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Hamster not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Хомяк не найден: id=42",
                                      "path": "/api/hamsters/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public HamsterResponse get(@Parameter(description = "ID хомяка") @PathVariable Long id) {
        return hamsterService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Переименовать хомяка", description = "По ID")
@ApiResponses({
            @ApiResponse(responseCode = "200", description = "Хомяк переименован"),

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
                                              "path": "/api/hamsters/1",
                                              "fieldErrors": {
                                                "name": "Имя обязательно"
                                              }
                                            }
                                            """),
                                    @ExampleObject(name = "Bad id", value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                              "path": "/api/hamsters/abc",
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
                    description = "Хомяк не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Hamster not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Хомяк не найден: id=42",
                                      "path": "/api/hamsters/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public HamsterResponse rename(@Parameter(description = "ID хомяка") @PathVariable Long id, @RequestBody @Valid HamsterRenameRequest req) {
        return hamsterService.rename(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Удалить хомяка", description = "По ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
@ApiResponses({
            @ApiResponse(responseCode = "204", description = "Хомяк удалён",
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
                                      "path": "/api/hamsters/abc",
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
                    description = "Хомяк не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Hamster not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Хомяк не найден: id=42",
                                      "path": "/api/hamsters/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public void delete(@Parameter(description = "ID хомяка") @PathVariable Long id) {
        hamsterService.delete(id);
    }
}