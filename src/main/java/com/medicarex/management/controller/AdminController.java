
package com.medicarex.management.controller;

import com.medicarex.management.repository.AppointmentRepository;
import com.medicarex.management.repository.DoctorRepository;
import com.medicarex.management.repository.UserRepository;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminController(
            UserRepository userRepository,
            DoctorRepository doctorRepository,
            AppointmentRepository appointmentRepository) {

        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {

        Map<String, Object> dashboard = new HashMap<>();

        // Main statistics
        dashboard.put("totalUsers", userRepository.count());
        dashboard.put("totalDoctors", doctorRepository.count());
        dashboard.put("totalAppointments", appointmentRepository.count());

        // Appointment status counts
        dashboard.put(
                "bookedAppointments",
                appointmentRepository.countByStatus("BOOKED")
        );

        dashboard.put(
                "acceptedAppointments",
                appointmentRepository.countByStatus("ACCEPTED")
        );

        dashboard.put(
                "rejectedAppointments",
                appointmentRepository.countByStatus("REJECTED")
        );

        dashboard.put(
                "completedAppointments",
                appointmentRepository.countByStatus("COMPLETED")
        );

        dashboard.put(
                "cancelledAppointments",
                appointmentRepository.countByStatus("CANCELLED")
        );

        return dashboard;
    }
}

