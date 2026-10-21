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

import com.academy.hamster_clinic.model.dto.HamsterDiseaseRequest;
import com.academy.hamster_clinic.model.dto.HamsterDiseaseResponse;
import com.academy.hamster_clinic.model.service.HamsterDiseaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @ResponseStatus(HttpStatus.CREATED)
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
    @Operation(summary = "Вывести список записей о болезнях",
           description = "Фильтрация по имени хомяка и названию болезни")
    public List<HamsterDiseaseResponse> list(
        @Parameter(description = "Имя хомяка", example = "Рыжик")
        @RequestParam(required = false) String hamsterName,
        @Parameter(description = "Название болезни", example = "Простуда")
        @RequestParam(required = false) String diseaseName) {
        return hamsterDiseaseService.list(hamsterName, diseaseName);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Вывести конкретную запись", description = "По ID")
    public HamsterDiseaseResponse get(@Parameter(description = "ID записи") @PathVariable Long id) {
        return hamsterDiseaseService.get(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить запись", description = "По ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID записи") @PathVariable Long id) {
        hamsterDiseaseService.delete(id);
    }
}
