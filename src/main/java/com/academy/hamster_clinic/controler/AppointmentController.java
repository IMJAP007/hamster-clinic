package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.model.dto.AppointmentRequest;
import com.academy.hamster_clinic.model.dto.AppointmentResponse;
import com.academy.hamster_clinic.model.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "Создать запись", description = "Создаёт новую запись. Нужен Врач, Хозяин, Хомяк хозяина, Врач на назначенное время должен быть свободен")
    public AppointmentResponse create(@RequestBody @Valid AppointmentRequest req) {
        return appointmentService.create(req);
    }

    @GetMapping
    @Operation(summary = "Выводит список записей", description = "Можно фильтровать по хомяку или врачу")
    public List<AppointmentResponse> list(
        @Parameter(description = "ID доктора") @RequestParam(required = false) Long doctorId, 
        @Parameter(description = "Id хозяина") @RequestParam(required = false) Long ownerId,
        @Parameter(description = "ID хомяка") @RequestParam(required = false) Long hamsterId
    ) {
        return appointmentService.list(doctorId, hamsterId, ownerId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Выводит конкретную запись", description = "По ID")
    public AppointmentResponse get(@Parameter(description = "ID записи") @PathVariable Long id) {
        return appointmentService.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удаляет запись", description = "По ID")
    public void cancel(@Parameter(description = "ID записи") @PathVariable Long id) {
        appointmentService.cancel(id);
    }
}
