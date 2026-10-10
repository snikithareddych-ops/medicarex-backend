
package com.medicarex.management.service;

import com.medicarex.management.dto.AppointmentResponseDTO;
import com.medicarex.management.entity.Appointment;
import com.medicarex.management.entity.Doctor;
import com.medicarex.management.entity.User;
import com.medicarex.management.repository.AppointmentRepository;
import com.medicarex.management.repository.DoctorRepository;
import com.medicarex.management.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            DoctorRepository doctorRepository) {

        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Appointment bookAppointment(
            Appointment appointment,
            String patientEmail) {

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        if (!"PATIENT".equalsIgnoreCase(patient.getRole())) {
            throw new IllegalArgumentException(
                    "Only patients can book appointments");
        }

        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        // Prevent new bookings with inactive doctors.
        if (!doctor.isActive()) {
            throw new IllegalArgumentException(
                    "This doctor is currently unavailable for new bookings");
        }

        List<Appointment> existingAppointments =
                appointmentRepository.findByDoctorId(doctor.getId());

        boolean duplicate = existingAppointments.stream().anyMatch(existing ->
                existing.getAppointmentDate().equals(
                        appointment.getAppointmentDate())
                && existing.getAppointmentTime().equals(
                        appointment.getAppointmentTime())
                && !"CANCELLED".equalsIgnoreCase(existing.getStatus())
                && !"REJECTED".equalsIgnoreCase(existing.getStatus()));

        if (duplicate) {
            throw new IllegalArgumentException(
                    "This doctor already has an appointment at that time");
        }

        appointment.setPatientId(patient.getId());
        appointment.setStatus("BOOKED");

        return appointmentRepository.save(appointment);
    }

    public List<AppointmentResponseDTO>
            getAllAppointmentsWithPatientDetails() {

        return appointmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<AppointmentResponseDTO>
            getAppointmentsByPatientIdWithPatientDetails(Long patientId) {

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<AppointmentResponseDTO>
            getAppointmentsByPatientEmail(String patientEmail) {

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        return appointmentRepository.findByPatientId(patient.getId())
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<AppointmentResponseDTO>
            getAppointmentsByDoctorIdWithPatientDetails(Long doctorId) {

        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<AppointmentResponseDTO> getAppointmentsByDoctorEmail(
            Long doctorId,
            String doctorEmail) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Doctor not found"));

        if (!doctor.getEmail().equalsIgnoreCase(doctorEmail)) {
            throw new IllegalArgumentException(
                    "You can view only your own appointments");
        }

        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    private AppointmentResponseDTO convertToDTO(
            Appointment appointment) {

        User patient = userRepository
                .findById(appointment.getPatientId())
                .orElse(null);

        String patientName =
                patient != null ? patient.getName() : "Unknown";

        String patientEmail =
                patient != null ? patient.getEmail() : "Unknown";

        Doctor doctor = doctorRepository
                .findById(appointment.getDoctorId())
                .orElse(null);

        String doctorName =
                doctor != null ? doctor.getName() : "Unknown";

        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getPatientId(),
                patientName,
                patientEmail,
                appointment.getDoctorId(),
                doctorName,
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }

    public Appointment getAppointmentById(Long id) {

        return appointmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Appointment not found"));
    }

    public Appointment updateAppointment(
            Long id,
            Appointment appointment) {

        Appointment existingAppointment = getAppointmentById(id);

        existingAppointment.setPatientId(appointment.getPatientId());
        existingAppointment.setDoctorId(appointment.getDoctorId());
        existingAppointment.setAppointmentDate(
                appointment.getAppointmentDate());
        existingAppointment.setAppointmentTime(
                appointment.getAppointmentTime());
        existingAppointment.setStatus(appointment.getStatus());

        return appointmentRepository.save(existingAppointment);
    }

    public Appointment updateAppointmentStatus(
            Long id,
            String status,
            Authentication authentication) {

        Appointment appointment = getAppointmentById(id);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        boolean isDoctor = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_DOCTOR"));

        if (!isAdmin && !isDoctor) {
            throw new IllegalArgumentException(
                    "You are not allowed to update appointment status");
        }

        if (isDoctor && !isAdmin) {

            String doctorEmail = authentication.getName();

            Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                    .orElseThrow(() ->
                            new IllegalArgumentException("Doctor not found"));

            if (!appointment.getDoctorId().equals(doctor.getId())) {
                throw new IllegalArgumentException(
                        "You can update only your own appointments");
            }
        }

        String currentStatus = appointment.getStatus();
        String newStatus = status.toUpperCase();

        if (!List.of(
                "BOOKED",
                "ACCEPTED",
                "REJECTED",
                "COMPLETED",
                "CANCELLED"
        ).contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid appointment status");
        }

        boolean validTransition =
                currentStatus.equalsIgnoreCase(newStatus)
                || (currentStatus.equalsIgnoreCase("BOOKED")
                    && List.of("ACCEPTED", "REJECTED", "CANCELLED")
                            .contains(newStatus))
                || (currentStatus.equalsIgnoreCase("ACCEPTED")
                    && newStatus.equals("COMPLETED"));

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid appointment status transition");
        }

        appointment.setStatus(newStatus);

        return appointmentRepository.save(appointment);
    }

    public Appointment cancelAppointment(
            Long appointmentId,
            String patientEmail) {

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Patient not found"));

        Appointment appointment = getAppointmentById(appointmentId);

        if (!appointment.getPatientId().equals(patient.getId())) {
            throw new IllegalArgumentException(
                    "You can cancel only your own appointments");
        }

        if (!appointment.getStatus().equalsIgnoreCase("BOOKED")) {
            throw new IllegalArgumentException(
                    "Only booked appointments can be cancelled");
        }

        appointment.setStatus("CANCELLED");

        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {

        if (!appointmentRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Appointment not found");
        }

        appointmentRepository.deleteById(id);
    }
}