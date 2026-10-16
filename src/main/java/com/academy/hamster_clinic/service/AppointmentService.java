package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.hamster_clinic.dto.AppointmentRequest;
import com.academy.hamster_clinic.dto.AppointmentResponse;
import com.academy.hamster_clinic.entity.Appointment;
import com.academy.hamster_clinic.entity.Doctor;
import com.academy.hamster_clinic.entity.Hamster;
import com.academy.hamster_clinic.entity.Owner;
import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.repository.AppointmentRepository;
import com.academy.hamster_clinic.repository.DoctorRepository;
import com.academy.hamster_clinic.repository.HamsterRepository;
import com.academy.hamster_clinic.repository.OwnerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final OwnerRepository ownerRepository;
    private final HamsterRepository hamsterRepository;

        @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        Doctor doctor = doctorRepository.findById(req.doctorId()).orElseThrow(
            () -> new ResourceNotFoundException("Врач не найден: id = " + req.doctorId()));
        
        Owner owner = ownerRepository.findById(req.ownerId()).orElseThrow(
            () -> new ResourceNotFoundException("Хозяин не найден: id = " + req.ownerId()));
        
        Hamster hamster = hamsterRepository.findById(req.hamsterId()).orElseThrow(
            () -> new ResourceNotFoundException("Хомяк не найден: id = " + req.hamsterId()));

        if (!hamster.getOwner().getId().equals(owner.getId())) {
            throw new BusinessException(
                    "Хомяк id=" + hamster.getId() + " не принадлежит хозяину id = " + owner.getId());
        }
        
        if (appointmentRepository.existsByDoctorIdAndAppointmentDate(doctor.getId(), req.appointmentDate())) {
            throw new BusinessException("Врач уже занят на " + req.appointmentDate());
        }

        Appointment a = new Appointment();
        
        a.setDoctor(doctor);
        a.setOwner(owner);
        a.setHamster(hamster);
        a.setAppointmentDate(req.appointmentDate());
        
        return toResponse(appointmentRepository.save(a));
    }

    public AppointmentResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<AppointmentResponse> list(Long doctorId, Long hamsterId) {
        List<Appointment> list;
        
        if (doctorId != null) {
            list = appointmentRepository.findByDoctorIdOrderByAppointmentDateAsc(doctorId);
        }
        else if (hamsterId != null) {
            list = appointmentRepository.findByHamsterIdOrderByAppointmentDateAsc(hamsterId);
        }
        else {
            list = appointmentRepository.findAll();
        }
        
        return list.stream().map(AppointmentService::toResponse).toList();
    }

    @Transactional
    public void cancel(Long id) {
        appointmentRepository.delete(findOrThrow(id));
    }

    private Appointment findOrThrow(Long id) {
        return appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Запись не найдена: id = " + id));
    }

    private static AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getDoctor().getId(), a.getDoctor().getFullName(), a.getDoctor().getSpecialty(),
                a.getOwner().getId(),  a.getOwner().getFullName(),
                a.getHamster().getId(), a.getHamster().getName(),
                a.getAppointmentDate(),
                a.getCreatedAt()
        );
    }
}
