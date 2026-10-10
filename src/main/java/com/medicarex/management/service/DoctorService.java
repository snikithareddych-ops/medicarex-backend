
package com.medicarex.management.service;

import com.medicarex.management.entity.Doctor;
import com.medicarex.management.entity.User;
import com.medicarex.management.repository.DoctorRepository;
import com.medicarex.management.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public DoctorService(
            DoctorRepository doctorRepository,
            UserRepository userRepository) {

        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Doctor addDoctor(Doctor doctor) {

        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "A doctor with this email already exists"
            );
        }

        if (userRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "A user account with this email already exists"
            );
        }

        if (doctor.getLoginPassword() == null ||
                doctor.getLoginPassword().trim().isEmpty()) {
            throw new RuntimeException(
                    "Doctor login password is required"
            );
        }

        User doctorUser = new User();
        doctorUser.setName(doctor.getName());
        doctorUser.setEmail(doctor.getEmail());
        doctorUser.setPassword(
                passwordEncoder.encode(doctor.getLoginPassword())
        );
        doctorUser.setRole("DOCTOR");

        userRepository.save(doctorUser);

        doctor.setLoginPassword(null);
        doctor.setActive(true);

        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
public List<Doctor> getActiveDoctors() {
    return doctorRepository.findByActiveTrue();
}
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found")
                );
    }

    public Doctor getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found")
                );
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor doctor) {

        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found")
                );

        String oldEmail = existingDoctor.getEmail();
        String newEmail = doctor.getEmail();

        User doctorUser = userRepository.findByEmail(oldEmail)
                .orElse(null);

        if (!oldEmail.equals(newEmail)) {

            if (doctorRepository.findByEmail(newEmail).isPresent()) {
                throw new RuntimeException(
                        "A doctor with this email already exists"
                );
            }

            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new RuntimeException(
                        "A user account with this email already exists"
                );
            }

            if (doctorUser != null &&
                    "DOCTOR".equals(doctorUser.getRole())) {
                doctorUser.setEmail(newEmail);
            }
        }

        if (doctorUser != null &&
                "DOCTOR".equals(doctorUser.getRole())) {

            doctorUser.setName(doctor.getName());
            userRepository.save(doctorUser);
        }

        existingDoctor.setName(doctor.getName());
        existingDoctor.setSpecialization(doctor.getSpecialization());
        existingDoctor.setEmail(newEmail);
        existingDoctor.setPhone(doctor.getPhone());
        existingDoctor.setExperience(doctor.getExperience());
        existingDoctor.setAvailableDays(doctor.getAvailableDays());
        existingDoctor.setAvailableTime(doctor.getAvailableTime());

        // Preserve the current active status during profile updates.
        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public Doctor deactivateDoctor(Long id) {

        Doctor doctor = getDoctorById(id);

        if (!doctor.isActive()) {
            return doctor;
        }

        doctor.setActive(false);

        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor activateDoctor(Long id) {

        Doctor doctor = getDoctorById(id);

        if (doctor.isActive()) {
            return doctor;
        }

        doctor.setActive(true);

        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {

        Doctor doctor = getDoctorById(id);

        User doctorUser = userRepository
                .findByEmail(doctor.getEmail())
                .orElse(null);

        if (doctorUser != null &&
                "DOCTOR".equals(doctorUser.getRole())) {
            userRepository.delete(doctorUser);
        }

        doctorRepository.delete(doctor);
    }
}