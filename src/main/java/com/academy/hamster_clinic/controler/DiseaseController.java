package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.model.dto.DiseaseRequest;
import com.academy.hamster_clinic.model.dto.DiseaseResponse;
import com.academy.hamster_clinic.model.service.DiseaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать болезнь", description = "Создаёт новую болезнь")
    public DiseaseResponse create(@RequestBody @Valid DiseaseRequest req) {
        return diseaseService.create(req);
    }

    @GetMapping
    @Operation(summary = "Вывести список болезней")
    public List<DiseaseResponse> list(@Parameter(description = "Наименование болезни", example = "Простуда") @RequestParam (required = false) String name) {
        return diseaseService.list(name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Вывести конкретную болезнь", description = "По ID")
    public DiseaseResponse get(@Parameter(description = "ID болезни") @PathVariable Long id) {
        return diseaseService.get(id);
    }
}
