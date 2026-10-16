package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.academy.hamster_clinic.dto.DoctorRequest;
import com.academy.hamster_clinic.dto.DoctorResponse;
import com.academy.hamster_clinic.service.DoctorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать врача", description = "Создаёт нового врача. Email должен быть уникальным.")
    public DoctorResponse create(@RequestBody @Valid DoctorRequest req) {
        return doctorService.create(req);
    }

    @GetMapping
    @Operation(summary = "Вывести список врачей", description = "Можно сортировать по специальности.")
    public List<DoctorResponse> list(@Parameter(description = "Специальнось врача") @RequestParam (required = false) String specialty) {
        return doctorService.list(specialty);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Вывести конкретного врача", description = "По ID")
    public DoctorResponse get(@Parameter(description = "ID доктора") @PathVariable Long id) {
        return doctorService.get(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные конкретного врача", description = "По ID")
    public DoctorResponse update(@Parameter(description = "ID доктора") @PathVariable Long id, @RequestBody @Valid DoctorRequest req) {
        return doctorService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить врача", description = "По ID")
    public void delete(@Parameter(description = "ID доктора") @PathVariable Long id) {
        doctorService.delete(id);
    }
}
