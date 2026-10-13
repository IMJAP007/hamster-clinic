package com.academy.hamster_clinic.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.hamster_clinic.entity.Disease;
import com.academy.hamster_clinic.entity.Hamster;
import com.academy.hamster_clinic.entity.HamsterDisease;
import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.repository.DiseaseRepository;
import com.academy.hamster_clinic.repository.HamsterDiseaseRepository;
import com.academy.hamster_clinic.repository.HamsterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HamsterDiseaseService {

    private final HamsterDiseaseRepository hamsterDiseaseRepository;
    private final HamsterRepository hamsterRepository;
    private final DiseaseRepository diseaseRepository;

    @Transactional
    public HamsterDisease diagnose(Long hamsterId, Long diseaseId, LocalDate diagnosedAt) {
        if (hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(hamsterId, diseaseId, diagnosedAt)) {
            throw new BusinessException(
                    "Такой диагноз уже установлен этому хомяку на " + diagnosedAt);
        }

        Hamster hamster = hamsterRepository.findById(hamsterId)
                .orElseThrow(() -> new ResourceNotFoundException("Хомяк не найден: id = " + hamsterId));
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Болезнь не найдена: id = " + diseaseId));

        HamsterDisease hd = new HamsterDisease();
        hd.setHamster(hamster);
        hd.setDisease(disease);
        hd.setDiagnosedAt(diagnosedAt);
        
        return hamsterDiseaseRepository.save(hd);
    }

    public List<HamsterDisease> listByHamster(Long hamsterId) {
        return hamsterDiseaseRepository.findByHamsterIdOrderByDiagnosedAtDesc(hamsterId);
    }

    public List<HamsterDisease> listByDisease(Long diseaseId) {
        return hamsterDiseaseRepository.findByDiseaseId(diseaseId);
    }

    @Transactional
    public void remove(Long id) {
        if (!hamsterDiseaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Запись диагноза не найдена: id = " + id);
        }

        hamsterDiseaseRepository.deleteById(id);
    }
}
