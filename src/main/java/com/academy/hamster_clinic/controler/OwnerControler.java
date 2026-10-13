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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.dto.OwnerRequest;
import com.academy.hamster_clinic.dto.OwnerResponse;
import com.academy.hamster_clinic.service.OwnerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerControler {
    
    private final OwnerService ownerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OwnerResponse create(@RequestBody @Valid OwnerRequest req) {
        return ownerService.create(req);
    }

    @GetMapping
    public List<OwnerResponse> get() {
        return ownerService.list();
    }

    @GetMapping("/{id}")
    public OwnerResponse get(@PathVariable Long id) {
        return ownerService.get(id);
    }

    @PutMapping("/{id}")
    public OwnerResponse update(@PathVariable Long id, @RequestBody @Valid OwnerRequest req) {
        return ownerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        ownerService.delete(id);
    }
    
}
