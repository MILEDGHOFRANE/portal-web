package com.dxc.auth.controller;

import com.dxc.auth.entity.User;
import com.dxc.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(Map.of("success", true, "users", users));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            try {
                user.setRole(User.Role.valueOf(body.get("role")));
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("success", true, "message", "Rôle mis à jour"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Rôle invalide"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return userRepository.findById(id).map(user -> {
            user.setIsActive((Boolean) body.get("isActive"));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Statut mis à jour"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Un compte avec cet email existe déjà"));
        }
        try {
            User user = new User();
            user.setEmail(email);
            user.setFirstName(body.get("firstName"));
            user.setLastName(body.get("lastName"));
            user.setPasswordHash(passwordEncoder.encode(body.get("password")));
            user.setRole(User.Role.valueOf(body.getOrDefault("role", "EMPLOYEE")));
            user.setIsVerified(true);
            user.setIsActive(true);
            user.setIsLocked(false);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Compte créé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<?> unlockUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setIsLocked(false);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true, "message", "Compte déverrouillé"));
        }).orElse(ResponseEntity.notFound().build());
    }
}
