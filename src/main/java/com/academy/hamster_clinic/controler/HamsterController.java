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

import com.academy.hamster_clinic.dto.HamsterRenameRequest;
import com.academy.hamster_clinic.dto.HamsterRequest;
import com.academy.hamster_clinic.dto.HamsterResponse;
import com.academy.hamster_clinic.service.HamsterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hamsters")
@RequiredArgsConstructor
public class HamsterController {

    private final HamsterService hamsterService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HamsterResponse create(@RequestBody @Valid HamsterRequest req) {
        return hamsterService.create(req);
    }

    @GetMapping
    public List<HamsterResponse> list(@RequestParam(required = false) Long ownerId) {
        return hamsterService.list(ownerId);
    }

    @GetMapping("/{id}")
    public HamsterResponse get(@PathVariable Long id) {
        return hamsterService.get(id);
    }

    @PatchMapping("/{id}")
    public HamsterResponse rename(@PathVariable Long id, @RequestBody @Valid HamsterRenameRequest req) {
        return hamsterService.rename(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        hamsterService.delete(id);
    }
}