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
import com.academy.hamster_clinic.model.dto.DoctorRequest;
import com.academy.hamster_clinic.model.dto.DoctorResponse;
import com.academy.hamster_clinic.model.dto.ErrorResponse;
import com.academy.hamster_clinic.model.service.DoctorService;

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
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Врачи", description = "Операции с врачами")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Врач создан"),

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
                                      "path": "/api/doctors",
                                      "fieldErrors": {
                                        "fullName": "ФИО обязательно",
                                        "specialty": "Специальность обязательна",
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
                    description = "Врач с таким email уже существует",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Duplicate email", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Врач с email уже существует: ivanov@vet.example",
                                      "path": "/api/doctors",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    @Operation(summary = "Создать врача", description = "Создаёт нового врача. Email должен быть уникальным.")
    public DoctorResponse create(@RequestBody @Valid DoctorRequest req) {
        return doctorService.create(req);
    }

    // @GetMapping
    // @Operation(summary = "Вывести список врачей", description = "Можно сортировать по специальности.")
    // public List<DoctorResponse> list(@Parameter(description = "Специальнось врача") @RequestParam (required = false) String specialty) {
    //     return doctorService.list(specialty);
    // }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Вывести список врачей", description = "Можно с фильтрцией по ФИО")
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
    public List<DoctorResponse> filterByFullName(@Parameter(description = "ФИО Врача", example = "Иванов Иван Иванович") @RequestParam (required = false) String fullName) {
        return doctorService.searchByFullName(fullName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Вывести конкретного врача", description = "По ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Врач найден"),

            // 400 — если в path не число: /api/doctors/abc
            @ApiResponse(responseCode = "400",
                    description = "Неверный тип параметра id (ожидается число)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad id", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                      "path": "/api/doctors/abc",
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
                    description = "Врач не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Doctor not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Врач не найден: id=42",
                                      "path": "/api/doctors/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public DoctorResponse get(@Parameter(description = "ID доктора") @PathVariable Long id) {
        return doctorService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить данные конкретного врача", description = "По ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Врач обновлён"),

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
                                              "path": "/api/doctors/1",
                                              "fieldErrors": {
                                                "specialty": "Специальность обязательна"
                                              }
                                            }
                                            """),
                                    @ExampleObject(name = "Bad id", value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                              "path": "/api/doctors/abc",
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
                    description = "Врач не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Doctor not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Врач не найден: id=42",
                                      "path": "/api/doctors/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "409",
                    description = "Новый email уже занят другим врачом",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Duplicate email", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Email уже занят: petrov@vet.example",
                                      "path": "/api/doctors/1",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public DoctorResponse update(@Parameter(description = "ID доктора") @PathVariable Long id, @RequestBody @Valid DoctorRequest req) {
        return doctorService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить врача", description = "По ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Врач удалён",
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
                                      "path": "/api/doctors/abc",
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
                    description = "Врач не найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Doctor not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Врач не найден: id=42",
                                      "path": "/api/doctors/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "409",
                    description = "У врача есть записи на приём — удалить нельзя (ON DELETE RESTRICT)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Doctor has appointments", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Нарушение целостности данных",
                                      "path": "/api/doctors/1",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public void delete(@Parameter(description = "ID доктора") @PathVariable Long id) {
        doctorService.delete(id);
    }
}
