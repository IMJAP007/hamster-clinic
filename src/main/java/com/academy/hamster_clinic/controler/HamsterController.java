package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.academy.hamster_clinic.model.dto.HamsterRenameRequest;
import com.academy.hamster_clinic.model.dto.HamsterRequest;
import com.academy.hamster_clinic.model.dto.HamsterResponse;
import com.academy.hamster_clinic.model.service.HamsterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать хомяка", description = "Создаёт нового хомяка. Нужен хозяин.")
    public HamsterResponse create(@RequestBody @Valid HamsterRequest req) {
        return hamsterService.create(req);
    }

    @GetMapping
    @Operation(summary = "Вывести список хомяков", description = "Можно с фильтрацией по владельцу")
    public List<HamsterResponse> list(@Parameter(description = "ID владельца") @RequestParam(required = false) Long ownerId) {
        return hamsterService.list(ownerId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Вывести хомяка", description = "По ID")
    public HamsterResponse get(@Parameter(description = "ID хомяка") @PathVariable Long id) {
        return hamsterService.get(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Переименовать хомяка", description = "По ID")
    public HamsterResponse rename(@Parameter(description = "ID хомяка") @PathVariable Long id, @RequestBody @Valid HamsterRenameRequest req) {
        return hamsterService.rename(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить хомяка", description = "По ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID хомяка") @PathVariable Long id) {
        hamsterService.delete(id);
    }
}