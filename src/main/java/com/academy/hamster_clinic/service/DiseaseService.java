package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Disease create(String name) {
        if (diseaseRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Болезнь уже существует: " + name);
        }

        Disease disease = new Disease();
        disease.setName(name);
        
        return diseaseRepository.save(disease);
    }

    public Disease getById(Long id) {
        return diseaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Болезнь не найдена: id = " + id));
    }

    public List<Disease> list() {
        return diseaseRepository.findAll();
    }
}
