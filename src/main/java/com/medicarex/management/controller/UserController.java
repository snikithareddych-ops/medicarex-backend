package com.medicarex.management.controller;

import com.medicarex.management.dto.LoginRequest;
import com.medicarex.management.dto.LoginResponse;
import com.medicarex.management.dto.UserResponseDTO;
import com.medicarex.management.entity.User;
import com.medicarex.management.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    // REGISTER

    @PostMapping("/register")
    public User registerUser(
            @RequestBody User user) {

        return userService.registerUser(user);
    }


    // GET ALL USERS

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {

        return userService.getAllUsers();
    }


    // GET USER BY EMAIL

    @GetMapping("/email")
    public UserResponseDTO getUserByEmail(
            @RequestParam String email) {

        User user = userService.getUserByEmail(email);

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }


    // LOGIN

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        return userService.login(request);
    }


    // RESET ADMIN PASSWORD
    // Development use only

    @PutMapping("/reset-admin-password")
    public String resetAdminPassword() {

        userService.resetAdminPassword();

        return "Admin password reset successfully";
    }


    // DELETE USER

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                "User deleted successfully"
        );
    }
}