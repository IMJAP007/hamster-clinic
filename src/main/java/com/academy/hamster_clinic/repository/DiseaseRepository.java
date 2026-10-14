package com.academy.hamster_clinic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.entity.Disease;

public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    
    Optional<Disease> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
