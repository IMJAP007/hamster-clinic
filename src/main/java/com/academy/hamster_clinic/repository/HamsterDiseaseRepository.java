package com.academy.hamster_clinic.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.entity.HamsterDisease;

public interface HamsterDiseaseRepository extends JpaRepository<HamsterDisease, Long> {
    
    List<HamsterDisease> findByHamsterIdOrderByDiagnosedAtDesc(Long hamsterId);
    List<HamsterDisease> findByDiseaseId(Long diseaseId);
    boolean existsByHamsterIdAndDiseaseIdAndDiagnosedAt(Long hamsterId, Long diseaseId, LocalDate diagnosedAt);
}
