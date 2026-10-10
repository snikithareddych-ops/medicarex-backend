
package com.medicarex.management.controller;

import com.medicarex.management.entity.Doctor;
import com.medicarex.management.service.DoctorService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@CrossOrigin(origins = "http://localhost:5173")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public Doctor addDoctor(@Valid @RequestBody Doctor doctor) {
        return doctorService.addDoctor(doctor);
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }
    @GetMapping("/active")
public List<Doctor> getActiveDoctors() {
    return doctorService.getActiveDoctors();
}

    @GetMapping("/{id}")
    public Doctor getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id);
    }

    @GetMapping("/email")
    public Doctor getDoctorByEmail(@RequestParam String email) {
        return doctorService.getDoctorByEmail(email);
    }

    @PutMapping("/{id}")
    public Doctor updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody Doctor doctor) {
        return doctorService.updateDoctor(id, doctor);
    }

    @PatchMapping("/{id}/deactivate")
    public Doctor deactivateDoctor(@PathVariable Long id) {
        return doctorService.deactivateDoctor(id);
    }

    @PatchMapping("/{id}/activate")
    public Doctor activateDoctor(@PathVariable Long id) {
        return doctorService.activateDoctor(id);
    }

    @DeleteMapping("/{id}")
    public String deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return "Doctor deleted successfully";
    }
}