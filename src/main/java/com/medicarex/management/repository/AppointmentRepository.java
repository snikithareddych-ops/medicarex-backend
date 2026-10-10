package com.medicarex.management.repository;

import com.medicarex.management.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(
            Long doctorId,
            java.time.LocalDate appointmentDate,
            java.time.LocalTime appointmentTime);

    long countByStatus(String status);
}