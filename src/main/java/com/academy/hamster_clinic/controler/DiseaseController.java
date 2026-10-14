package com.academy.hamster_clinic.controler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.academy.hamster_clinic.dto.DiseaseRequest;
import com.academy.hamster_clinic.dto.DiseaseResponse;
import com.academy.hamster_clinic.service.DiseaseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiseaseResponse create(@RequestBody @Valid DiseaseRequest req) {
        return diseaseService.create(req);
    }

    @GetMapping
    public List<DiseaseResponse> list() {
        return diseaseService.list();
    }

    @GetMapping("/{id}")
    public DiseaseResponse get(@PathVariable Long id) {
        return diseaseService.get(id);
    }
}
