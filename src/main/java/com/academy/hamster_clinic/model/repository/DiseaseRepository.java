package com.academy.hamster_clinic.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.model.entity.Disease;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    
    List<Disease> findByNameContainingIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
