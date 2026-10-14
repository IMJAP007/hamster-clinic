package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.hamster_clinic.dto.DiseaseRequest;
import com.academy.hamster_clinic.dto.DiseaseResponse;
import com.academy.hamster_clinic.entity.Disease;
import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.repository.DiseaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    @Transactional
    public DiseaseResponse create(DiseaseRequest req) {
        if (diseaseRepository.existsByNameIgnoreCase(req.name())) {
            throw new BusinessException("Болезнь уже существует: " + req.name());
        }
        
        Disease d = new Disease();
        d.setName(req.name());
        
        return toResponse(diseaseRepository.save(d));
    }

    public DiseaseResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<DiseaseResponse> list() {
        return diseaseRepository.findAll().stream().map(DiseaseService::toResponse).toList();
    }

    private Disease findOrThrow(Long id) {
        return diseaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Болезнь не найдена: id = " + id));
    }

    private static DiseaseResponse toResponse(Disease d) {
        return new DiseaseResponse(d.getId(), d.getName());
    }
}
