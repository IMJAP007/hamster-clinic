package com.academy.hamster_clinic.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.model.entity.Disease;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    
    Optional<Disease> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
