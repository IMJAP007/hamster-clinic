package com.academy.hamster_clinic.model.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.model.dto.HamsterDiseaseRequest;
import com.academy.hamster_clinic.model.dto.HamsterDiseaseResponse;
import com.academy.hamster_clinic.model.entity.Disease;
import com.academy.hamster_clinic.model.entity.Hamster;
import com.academy.hamster_clinic.model.entity.HamsterDisease;
import com.academy.hamster_clinic.model.repository.DiseaseRepository;
import com.academy.hamster_clinic.model.repository.HamsterDiseaseRepository;
import com.academy.hamster_clinic.model.repository.HamsterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HamsterDiseaseService {

    private final HamsterDiseaseRepository hamsterDiseaseRepository;
    private final HamsterRepository hamsterRepository;
    private final DiseaseRepository diseaseRepository;

    @Transactional
    public HamsterDiseaseResponse create(HamsterDiseaseRequest req) {
        if (hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(req.hamsterId(), req.diseaseId(), req.diagnosedAt())) {
            throw new BusinessException("Такой диагноз уже установлен на " + req.diagnosedAt());
        }
        
        Hamster hamster = hamsterRepository.findById(req.hamsterId()).orElseThrow(
            ()-> new ResourceNotFoundException("Хомяк не найден: id = " + req.hamsterId()));
        
        Disease disease = diseaseRepository.findById(req.diseaseId()).orElseThrow(
            ()-> new ResourceNotFoundException("Болезнь не найдена: id = " + req.diseaseId()));

        HamsterDisease hd = new HamsterDisease();
        
        hd.setHamster(hamster);
        hd.setDisease(disease);
        hd.setDiagnosedAt(req.diagnosedAt());
        
        return toResponse(hamsterDiseaseRepository.save(hd));
    }

    public HamsterDiseaseResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<HamsterDiseaseResponse> list(Long hamsterId, Long diseaseId) {
        // List<HamsterDisease> list;
        // if (hamsterId != null) {
        //     list = hamsterDiseaseRepository.findByHamsterIdOrderByDiagnosedAtDesc(hamsterId);
        // }
        // else if (diseaseId != null) {
        //     list = hamsterDiseaseRepository.findByDiseaseId(diseaseId);
        // }
        // else {
        //     list = hamsterDiseaseRepository.findAll();
        // }
        
        // return list.stream().map(HamsterDiseaseService::toResponse).toList();

        return hamsterDiseaseRepository.findByFilters(hamsterId, diseaseId).stream().map(HamsterDiseaseService::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        hamsterDiseaseRepository.delete(findOrThrow(id));
    }

    private HamsterDisease findOrThrow(Long id) {
        return hamsterDiseaseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Запись диагноза не найдена: id = " + id));
    }

    private static HamsterDiseaseResponse toResponse(HamsterDisease hd) {
        return new HamsterDiseaseResponse(
                hd.getId(),
                hd.getHamster().getId(), hd.getHamster().getName(),
                hd.getDisease().getId(), hd.getDisease().getName(),
                hd.getDiagnosedAt()
        );
    }
}
