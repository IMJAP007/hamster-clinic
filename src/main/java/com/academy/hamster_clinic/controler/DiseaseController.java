package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.config.ApiErrorExamples;
import com.academy.hamster_clinic.model.dto.DiseaseRequest;
import com.academy.hamster_clinic.model.dto.DiseaseResponse;
import com.academy.hamster_clinic.model.dto.ErrorResponse;
import com.academy.hamster_clinic.model.service.DiseaseService;

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
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
@Tag(name = "Болезни", description = "Операции с болезнями")
public class DiseaseController {

    private final DiseaseService diseaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать болезнь", description = "Создаёт новую болезнь")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Болезнь создана"),

            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации (пустое название или длиннее 200 символов)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Validation failed", value = """
                                    {
                                      "timestamp": "2026-10-23T12:00:00+03:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Ошибка валидации",
                                      "path": "/api/diseases",
                                      "fieldErrors": {
                                        "name": "Название обязательно"
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
                    description = "Болезнь с таким названием уже существует (без учёта регистра)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Duplicate name", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Болезнь уже существует: Ожирение",
                                      "path": "/api/diseases",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public DiseaseResponse create(@RequestBody @Valid DiseaseRequest req) {
        return diseaseService.create(req);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Вывести список болезней")
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
    public List<DiseaseResponse> list(@Parameter(description = "Наименование болезни", example = "Простуда") @RequestParam (required = false) String name) {
        return diseaseService.list(name);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Вывести конкретную болезнь", description = "По ID")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Болезнь найдена"),

            @ApiResponse(responseCode = "400",
                    description = "Неверный тип параметра id (ожидается число)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad id", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                      "path": "/api/diseases/abc",
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
                    description = "Недостаточно прав (нужна роль ADMIN)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.FORBIDDEN))),

            @ApiResponse(responseCode = "404",
                    description = "Болезнь не найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Disease not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Болезнь не найдена: id=42",
                                      "path": "/api/diseases/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public DiseaseResponse get(@Parameter(description = "ID болезни") @PathVariable Long id) {
        return diseaseService.get(id);
    }
}
