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

import com.academy.hamster_clinic.dto.HamsterDiseaseRequest;
import com.academy.hamster_clinic.dto.HamsterDiseaseResponse;
import com.academy.hamster_clinic.service.HamsterDiseaseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hamster-diseases")
@RequiredArgsConstructor
public class HamsterDiseaseController {

    private final HamsterDiseaseService hamsterDiseaseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HamsterDiseaseResponse create(@RequestBody @Valid HamsterDiseaseRequest req) {
        return hamsterDiseaseService.create(req);
    }

    @GetMapping
    public List<HamsterDiseaseResponse> list(@RequestParam(required = false) Long hamsterId, @RequestParam(required = false) Long diseaseId) {
        return hamsterDiseaseService.list(hamsterId, diseaseId);
    }

    @GetMapping("/{id}")
    public HamsterDiseaseResponse get(@PathVariable Long id) {
        return hamsterDiseaseService.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        hamsterDiseaseService.delete(id);
    }
}
