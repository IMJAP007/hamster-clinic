package com.academy.hamster_clinic.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByDoctorIdOrderByAppointmentDateAsc(Long doctorId);
    List<Appointment> findByHamsterIdOrderByAppointmentDateAsc(Long hamsterId);
    List<Appointment> findByOwnerIdOrderByAppointmentDateAsc(Long ownerId);
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDateTime from, LocalDateTime to);
    boolean existsByDoctorIdAndAppointmentDate(Long doctorId, LocalDateTime appointmentDate);
}
