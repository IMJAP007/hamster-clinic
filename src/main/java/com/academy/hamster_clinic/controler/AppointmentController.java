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
import com.academy.hamster_clinic.model.dto.AppointmentRequest;
import com.academy.hamster_clinic.model.dto.AppointmentResponse;
import com.academy.hamster_clinic.model.dto.ErrorResponse;
import com.academy.hamster_clinic.model.service.AppointmentService;

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
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Записи", description = "Операции с записями")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Создать запись", description = "Создаёт новую запись. Нужен Врач, Хозяин, Хомяк хозяина, Врач на назначенное время должен быть свободен")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Запись создана"),
        @ApiResponse(responseCode = "400",
                description = "Ошибка валидации или некорректный формат appointmentDate",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(name = "Validation failed", value = """
                                        {
                                            "timestamp": "2026-10-23T12:00:00+03:00",
                                            "status": 400,
                                            "error": "Bad Request",
                                            "message": "Ошибка валидации",
                                            "path": "/api/appointments",
                                            "fieldErrors": {
                                            "doctorId": "must not be null",
                                            "appointmentDate": "must not be null"
                                            }
                                        }
                                        """),
                                @ExampleObject(name = "Bad date format", value = """
                                        {
                                            "timestamp": "2026-10-23T12:00:00+03:00",
                                            "status": 400,
                                            "error": "Bad Request",
                                            "message": "Некорректное значение поля 'appointmentDate': 2026-10-16. Ожидается формат 2026-10-16T10:30:00.",
                                            "path": "/api/appointments",
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
                description = "Врач, хозяин или хомяк не найден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(name = "Doctor not found", value = """
                                        {
                                            "status": 404,
                                            "error": "Not Found",
                                            "message": "Врач не найден: id=99",
                                            "path": "/api/appointments",
                                            "fieldErrors": null
                                        }
                                        """),
                                @ExampleObject(name = "Hamster not found", value = """
                                        {
                                            "status": 404,
                                            "error": "Not Found",
                                            "message": "Хомяк не найден: id=99",
                                            "path": "/api/appointments",
                                            "fieldErrors": null
                                        }
                                        """)
                        })),

        @ApiResponse(responseCode = "409",
                description = "Врач уже занят на это время или хомяк не принадлежит указанному хозяину",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                                @ExampleObject(name = "Doctor busy", value = """
                                        {
                                            "status": 409,
                                            "error": "Conflict",
                                            "message": "Врач уже занят на 2026-11-01T10:30",
                                            "path": "/api/appointments",
                                            "fieldErrors": null
                                        }
                                        """),
                                @ExampleObject(name = "Wrong owner", value = """
                                        {
                                            "status": 409,
                                            "error": "Conflict",
                                            "message": "Хомяк id=1 не принадлежит хозяину id=2",
                                            "path": "/api/appointments",
                                            "fieldErrors": null
                                        }
                                        """)
                        })),

        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public AppointmentResponse create(@RequestBody @Valid AppointmentRequest req) {
        return appointmentService.create(req);
    }

    // @GetMapping
    // @Operation(summary = "Выводит список записей", description = "Можно фильтровать по хомяку или врачу")
    // public List<AppointmentResponse> list(
    //     @Parameter(description = "ID доктора") @RequestParam(required = false) Long doctorId, 
    //     @Parameter(description = "Id хозяина") @RequestParam(required = false) Long ownerId,
    //     @Parameter(description = "ID хомяка") @RequestParam(required = false) Long hamsterId
    // ) {
    //     return appointmentService.list(doctorId, hamsterId, ownerId);
    // }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR')")
    @Operation(summary = "Вывести список записей", description = "Фильтрация по ФИО врача, ФИО владельца и имени хомяка;")
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

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public List<AppointmentResponse> list(
        @Parameter(description = "ФИО врача", example = "Иванов Иван Иванович")
        @RequestParam(required = false) String doctorFullName,
        @Parameter(description = "ФИО владельца", example = "Иванов Иван Иванович")
        @RequestParam(required = false) String ownerFullName,
        @Parameter(description = "Имя хомяка", example = "Рыжик")
        @RequestParam(required = false) String hamsterName) {
            return appointmentService.list(doctorFullName, ownerFullName, hamsterName);
        }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR')")
    @Operation(summary = "Выводит конкретную запись", description = "По ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Запись найдена"),

            @ApiResponse(responseCode = "401",
                description = "Токен отсутствует, истёк или невалиден",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = {
                            @ExampleObject(name = "Unauthorized", value = ApiErrorExamples.UNAUTHORIZED),
                            @ExampleObject(name = "Token expired", value = ApiErrorExamples.TOKEN_EXPIRED),
                            @ExampleObject(name = "Invalid token", value = ApiErrorExamples.INVALID_TOKEN)
                        })),

            @ApiResponse(responseCode = "404",
                    description = "Запись не найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Appointment not found", value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Запись не найдена: id=42",
                                      "path": "/api/appointments/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    public AppointmentResponse get(@Parameter(description = "ID записи") @PathVariable Long id) {
        return appointmentService.get(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Запись отменена",
                    content = @Content),

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
                    description = "Запись не найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Запись не найдена: id=42",
                                      "path": "/api/appointments/42",
                                      "fieldErrors": null
                                    }
                                    """))),

            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ApiErrorExamples.INTERNAL_ERROR)))
    })
    @Operation(summary = "Удаляет запись", description = "По ID")
    public void cancel(@Parameter(description = "ID записи") @PathVariable Long id) {
        appointmentService.cancel(id);
    }
}
