package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.config.ApiErrorExamples;
import com.academy.hamster_clinic.model.dto.ErrorResponse;
import com.academy.hamster_clinic.model.dto.HamsterDiseaseRequest;
import com.academy.hamster_clinic.model.dto.HamsterDiseaseResponse;
import com.academy.hamster_clinic.model.service.HamsterDiseaseService;

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
@RequestMapping("/api/hamster-diseases")
@RequiredArgsConstructor
@Tag(name = "Болезни хомяков", description = "Тут хомякам ставятся диагнозы")
public class HamsterDiseaseController {

    private final HamsterDiseaseService hamsterDiseaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Диагноз установлен"),

            @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации тела или некорректный формат diagnosedAt",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Validation failed", value = """
                                            {
                                              "timestamp": "2026-10-23T12:00:00+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Ошибка валидации",
                                              "path": "/api/hamster-diseases",
                                              "fieldErrors": {
                                                "hamsterId": "must not be null",
                                                "diseaseId": "must not be null",
                                                "diagnosedAt": "must not be null"
                                              }
                                            }
                                            """),
                                    @ExampleObject(name = "Bad date format", value = """
                                            {
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Некорректное значение поля 'diagnosedAt': 23.10.2026. Ожидается формат ISO 8601 (yyyy-MM-dd).",
                                              "path": "/api/hamster-diseases",
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
                    description = "Хомяк или болезнь не найдены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Hamster not found", value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Хомяк не найден: id=99",
                                              "path": "/api/hamster-diseases",
                                              "fieldErrors": null
                                            }
                                            """),
                                    @ExampleObject(name = "Disease not found", value = """
                                            {
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Болезнь не найдена: id=99",
                                              "path": "/api/hamster-diseases",
                                              "fieldErrors": null
                                            }
                                            """)
                            })),

            @ApiResponse(responseCode = "409",
                    description = "Такой диагноз уже установлен этому хомяку на эту дату",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Duplicate diagnosis", value = """
                                    {
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Такой диагноз уже установлен на 2026-10-15",
                                      "path": "/api/hamster-diseases",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    @Operation(summary = "Создать запись о диагнозе", description = "Создаёт запись об установке диагноза хомяку. Нужен хомяк, больезнь и дата установки диагноза")
    public HamsterDiseaseResponse create(@RequestBody @Valid HamsterDiseaseRequest req) {
        return hamsterDiseaseService.create(req);
    }

    // @GetMapping
    // @Operation(summary = "Вывести список записей о болезнях", description = "Можно сортировать по хомяку или по болезни")
    // public List<HamsterDiseaseResponse> list(@Parameter(description = "ID хомяка") @RequestParam(required = false) Long hamsterId, @Parameter(description = "ID болезни") @RequestParam(required = false) Long diseaseId) {
    //     return hamsterDiseaseService.list(hamsterId, diseaseId);
    // }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Вывести список записей о болезнях",
           description = "Фильтрация по имени хомяка и названию болезни")
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
    public List<HamsterDiseaseResponse> list(
        @Parameter(description = "Имя хомяка", example = "Рыжик")
        @RequestParam(required = false) String hamsterName,
        @Parameter(description = "Название болезни", example = "Простуда")
        @RequestParam(required = false) String diseaseName) {
        return hamsterDiseaseService.list(hamsterName, diseaseName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Вывести конкретную запись", description = "По ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запись найдена"),

            @ApiResponse(responseCode = "400",
                    description = "Неверный тип параметра id (ожидается число)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Bad id", value = """
                                    {
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Параметр 'id' имеет неверный тип. Ожидается: Long",
                                      "path": "/api/hamster-diseases/abc",
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
                    description = "Запись о диагнозе не найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "HamsterDisease not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Запись диагноза не найдена: id=42",
                                      "path": "/api/hamster-diseases/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public HamsterDiseaseResponse get(@Parameter(description = "ID записи") @PathVariable Long id) {
        return hamsterDiseaseService.get(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Удалить запись", description = "По ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Запись удалена",
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
                                      "path": "/api/hamster-diseases/abc",
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
                    description = "Запись о диагнозе не найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "HamsterDisease not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Запись диагноза не найдена: id=42",
                                      "path": "/api/hamster-diseases/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public void delete(@Parameter(description = "ID записи") @PathVariable Long id) {
        hamsterDiseaseService.delete(id);
    }
}
