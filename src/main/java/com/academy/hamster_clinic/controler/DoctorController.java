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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(@RequestBody @Valid DoctorRequest req) {
        return doctorService.create(req);
    }

    @GetMapping
    public List<DoctorResponse> list(@RequestParam (required = false) String specialty) {
        return doctorService.list(specialty);
    }

    @GetMapping("/{id}")
    public DoctorResponse get(@PathVariable Long id) {
        return doctorService.get(id);
    }

    @PutMapping("/{id}")
    public DoctorResponse update(@PathVariable Long id, @RequestBody @Valid DoctorRequest req) {
        return doctorService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        doctorService.delete(id);
    }
}
