package com.academy.hamster_clinic.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Appointment create(Long doctorId, Long ownerId, Long hamsterId, LocalDateTime dateTime) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(() -> new ResourceNotFoundException("Врач не найден: id = " + doctorId));
        Owner owner = ownerRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Хозяин не найден: id = " + ownerId));
        Hamster hamster = hamsterRepository.findById(hamsterId).orElseThrow(() -> new ResourceNotFoundException("Хомяк не найден: id = " + hamsterId));

        if (!hamster.getOwner().getId().equals(ownerId)) {
            throw new BusinessException("Хомяк с id = " + hamsterId + " не принадлежит хозяину id = " + ownerId);
        }

        if (appointmentRepository.existsByDoctorIdAndAppointmentDate(doctorId, dateTime)) {
            throw new BusinessException("Врач уже занят на " + dateTime);
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setOwner(owner);
        appointment.setHamster(hamster);
        appointment.setAppointmentDate(dateTime);
        
        return appointmentRepository.save(appointment);
    }

    public Appointment getById(Long id) {
        return appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Запись не найдена: id = " + id));
    }

    public List<Appointment> listByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateAsc(doctorId);
    }

    public List<Appointment> listByHamster(Long hamsterId) {
        return appointmentRepository.findByHamsterIdOrderByAppointmentDateAsc(hamsterId);
    }

    @Transactional
    public void cancel(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Запись не найдена: id=" + id);
        }

        appointmentRepository.deleteById(id);
    }
}
