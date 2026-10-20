package com.academy.hamster_clinic.model.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.academy.hamster_clinic.model.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // List<Appointment> findByDoctorIdOrderByAppointmentDateAsc(Long doctorId);
    // List<Appointment> findByHamsterIdOrderByAppointmentDateAsc(Long hamsterId);
    // List<Appointment> findByOwnerIdOrderByAppointmentDateAsc(Long ownerId);

    // @Query("""
    //     SELECT a FROM Appointment a
    //     WHERE (:doctorId  IS NULL OR a.doctor.id  = :doctorId)
    //     AND (:hamsterId IS NULL OR a.hamster.id = :hamsterId)
    //     AND (:ownerId   IS NULL OR a.owner.id   = :ownerId)
    //     ORDER BY a.appointmentDate ASC
    //     """)
    // List<Appointment> findByFilters(@Param("doctorId") Long doctorId, @Param("hamsterId") Long hamsterId, @Param("ownerId") Long ownerId);

    @Query("""
        SELECT a FROM Appointment a
        JOIN FETCH a.doctor d
        JOIN FETCH a.owner o
        JOIN FETCH a.hamster h
        WHERE (:doctorFullName IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', CAST(:doctorFullName AS string), '%')))
        AND (:ownerFullName IS NULL OR LOWER(o.fullName) LIKE LOWER(CONCAT('%', CAST(:ownerFullName AS string), '%')))
        AND (:hamsterName IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', CAST(:hamsterName AS string), '%')))
        ORDER BY a.appointmentDate ASC
    """)
    List<Appointment> findByFilters(@Param("doctorFullName") String doctorFullName, @Param("ownerFullName")  String ownerFullName, @Param("hamsterName")    String hamsterName);

    List<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDateTime from, LocalDateTime to);
    boolean existsByDoctorIdAndAppointmentDate(Long doctorId, LocalDateTime appointmentDate);
}
