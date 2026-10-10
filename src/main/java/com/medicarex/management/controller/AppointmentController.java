package com.medicarex.management.controller;

import com.medicarex.management.dto.AppointmentResponseDTO;
import com.medicarex.management.entity.Appointment;
import com.medicarex.management.service.AppointmentService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "http://localhost:5173")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(
            AppointmentService appointmentService) {

        this.appointmentService = appointmentService;
    }


    // =========================
    // BOOK APPOINTMENT
    // =========================

    @PostMapping
    public Appointment bookAppointment(
            @RequestBody Appointment appointment,
            Authentication authentication) {

        String patientEmail =
                authentication.getName();

        return appointmentService.bookAppointment(
                appointment,
                patientEmail);
    }


    // =========================
    // ADMIN - ALL APPOINTMENTS
    // =========================

    @GetMapping
    public List<AppointmentResponseDTO>
    getAllAppointments() {

        return appointmentService
                .getAllAppointmentsWithPatientDetails();
    }


    // =========================
    // PATIENT - OWN APPOINTMENTS
    // =========================

    @GetMapping("/patient/{patientId}")
    public List<AppointmentResponseDTO>
    getAppointmentsByPatientId(
            @PathVariable Long patientId,
            Authentication authentication) {

        String patientEmail =
                authentication.getName();

        return appointmentService
                .getAppointmentsByPatientEmail(
                        patientEmail);
    }


    // =========================
    // DOCTOR - OWN APPOINTMENTS
    // =========================

    @GetMapping("/doctor/{doctorId}")
    public List<AppointmentResponseDTO>
    getAppointmentsByDoctorId(
            @PathVariable Long doctorId,
            Authentication authentication) {

        String doctorEmail =
                authentication.getName();

        return appointmentService
                .getAppointmentsByDoctorEmail(
                        doctorId,
                        doctorEmail);
    }


    // =========================
    // GET APPOINTMENT BY ID
    // =========================

    @GetMapping("/{id}")
    public Appointment getAppointmentById(
            @PathVariable Long id) {

        return appointmentService
                .getAppointmentById(id);
    }


    // =========================
    // UPDATE APPOINTMENT
    // =========================

    @PutMapping("/{id}")
    public Appointment updateAppointment(
            @PathVariable Long id,
            @RequestBody Appointment appointment) {

        return appointmentService.updateAppointment(
                id,
                appointment);
    }


    // =========================
    // UPDATE STATUS
    // =========================

    @PutMapping("/{id}/status")
    public Appointment updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {

        return appointmentService
                .updateAppointmentStatus(
                        id,
                        status,
                        authentication);
    }


    // =========================
    // CANCEL APPOINTMENT
    // =========================

    @PutMapping("/{id}/cancel")
    public Appointment cancelAppointment(
            @PathVariable Long id,
            Authentication authentication) {

        String patientEmail =
                authentication.getName();

        return appointmentService.cancelAppointment(
                id,
                patientEmail);
    }


    // =========================
    // DELETE APPOINTMENT
    // =========================

    @DeleteMapping("/{id}")
    public String deleteAppointment(
            @PathVariable Long id) {

        appointmentService.deleteAppointment(id);

        return "Appointment deleted successfully";
    }
}