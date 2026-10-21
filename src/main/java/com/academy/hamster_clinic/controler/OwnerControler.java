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

import com.academy.hamster_clinic.model.dto.OwnerRequest;
import com.academy.hamster_clinic.model.dto.OwnerResponse;
import com.academy.hamster_clinic.model.service.OwnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
@Tag(name = "Хозяева", description = "Операции с владельцами хомяков")
public class OwnerControler {
    
    private final OwnerService ownerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Создать хозяина", description = "Создаёт нового хозяина. Email должен быть уникальным.")
    public OwnerResponse create(@RequestBody @Valid OwnerRequest req) {
        return ownerService.create(req);
    }

    // @GetMapping
    // @Operation(summary = "Вывести владельцев", description = "Выводит список всех существующих владельцев.")
    // public List<OwnerResponse> get() {
    //     return ownerService.list();
    // }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Вывести владельцев", description = "Можно с фильтрацией по ФИО")
    public List<OwnerResponse> get(@Parameter(description = "ФИО Владельца", example = "Иванов Иван Иванович") @RequestParam (required = false) String fullName) {
        return ownerService.findByFullName(fullName);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Получить хозяина по ID")
    public OwnerResponse get(@Parameter(description = "ID владельца") @PathVariable Long id) {
        return ownerService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Обновить данные о владельце", description = "Получает владельца по ID и обновляет его данные.")
    public OwnerResponse update(@Parameter(description = "ID владельца") @PathVariable Long id, @RequestBody @Valid OwnerRequest req) {
        return ownerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Удалить владельца", description = "Получает владельца по ID и оудаляет его.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID владельца") @PathVariable Long id) {
        ownerService.delete(id);
    }
    
}
