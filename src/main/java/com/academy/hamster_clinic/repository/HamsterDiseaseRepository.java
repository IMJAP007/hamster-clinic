package com.academy.hamster_clinic.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academy.hamster_clinic.entity.HamsterDisease;

public interface HamsterDiseaseRepository extends JpaRepository<HamsterDisease, Long> {
    
    // List<HamsterDisease> findByHamsterIdOrderByDiagnosedAtDesc(Long hamsterId);
    // List<HamsterDisease> findByDiseaseId(Long diseaseId);

    @Query("""
            SELECT hd FROM HamsterDisease hd
            WHERE (:hamsterId IS NULL OR hd.hamster.id = :hamsterId)
              AND (:diseaseId IS NULL OR hd.disease.id = :diseaseId)
            ORDER BY hd.diagnosedAt DESC
            """)
    List<HamsterDisease> findByFilters(@Param("hamsterId") Long hamsterId, @Param("diseaseId") Long diseaseId);

    boolean existsByHamsterIdAndDiseaseIdAndDiagnosedAt(Long hamsterId, Long diseaseId, LocalDate diagnosedAt);
}
