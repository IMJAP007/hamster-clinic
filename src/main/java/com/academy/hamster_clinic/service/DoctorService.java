package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.hamster_clinic.dto.DoctorRequest;
import com.academy.hamster_clinic.dto.DoctorResponse;
import com.academy.hamster_clinic.entity.Doctor;
import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional
    public DoctorResponse create(DoctorRequest req) {
        if (req.email() != null && doctorRepository.existsByEmail(req.email())) {
            throw new BusinessException("Врач с email уже существует: " + req.email());
        }
        
        Doctor d = new Doctor();
        
        d.setFullName(req.fullName());
        d.setSpecialty(req.specialty());
        d.setPhone(req.phone());
        d.setEmail(req.email());
        
        return toResponse(doctorRepository.save(d));
    }

    public DoctorResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<DoctorResponse> list(String specialty) {
        List<Doctor> doctors = (specialty == null || specialty.isBlank()) ? doctorRepository.findAll() : doctorRepository.findBySpecialtyIgnoreCase(specialty);
        
        return doctors.stream().map(DoctorService::toResponse).toList();
    }

    @Transactional
    public DoctorResponse update(Long id, DoctorRequest req) {
        Doctor d = findOrThrow(id);
        
        if (req.email() != null && !req.email().equals(d.getEmail()) && doctorRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email уже занят: " + req.email());
        }
        
        d.setFullName(req.fullName());
        d.setSpecialty(req.specialty());
        d.setPhone(req.phone());
        d.setEmail(req.email());
        
        return toResponse(d);
    }

    @Transactional
    public void delete(Long id) {
        doctorRepository.delete(findOrThrow(id));
    }

    private Doctor findOrThrow(Long id) {
        return doctorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Врач не найден: id = " + id));
    }

    private static DoctorResponse toResponse(Doctor d) {
        return new DoctorResponse(d.getId(), d.getFullName(), d.getSpecialty(), d.getPhone(), d.getEmail(), d.getCreatedAt());
    }
}