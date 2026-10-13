package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Doctor create(Doctor doctor) {
        if (doctor.getEmail() != null && doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new BusinessException("Врач с таким email уже существует: " + doctor.getEmail());
        }
        
        return doctorRepository.save(doctor);
    }

    public Doctor getById(Long id) {
        return doctorRepository.findById(id) .orElseThrow(() -> new ResourceNotFoundException("Врач не найден: id = " + id));
    }

    public List<Doctor> list() {
        return doctorRepository.findAll();
    }

    public List<Doctor> listBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }

    @Transactional
    public Doctor update(Long id, String fullName, String specialty, String phone, String email) {
        Doctor doctor = getById(id);
        doctor.setFullName(fullName);
        doctor.setSpecialty(specialty);
        doctor.setPhone(phone);
        doctor.setEmail(email);
        
        return doctor;
    }

    @Transactional
    public void delete(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Врач не найден: id = " + id);
        }
        
        doctorRepository.deleteById(id);
    }
}