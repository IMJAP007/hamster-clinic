package com.academy.hamster_clinic.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.model.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
    Optional<Doctor> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Doctor> findByFullNameContaining(String fullName);
}