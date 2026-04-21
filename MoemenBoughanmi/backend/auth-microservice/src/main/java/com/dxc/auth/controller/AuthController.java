package com.dxc.auth.controller;

import com.dxc.auth.dto.AuthDtos;
import com.dxc.auth.entity.User;
import com.dxc.auth.service.AuthService;
import com.dxc.auth.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDtos.RegisterRequest request) {
        log.info("Tentative d'inscription: {}", request.getEmail());
        try {
            authService.register(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Inscription réussie! Vérifiez votre email."
            ));
        } catch (Exception e) {
            log.error("Erreur inscription: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            authService.verifyEmailToken(token);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email vérifié avec succès!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDtos.LoginRequest request) {
        log.info("Tentative de connexion: {}", request.getEmail());
        try {
            AuthDtos.AuthResponse response = authService.login(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("requiresOtp", response.isRequiresOtp());
            
            if (!response.isRequiresOtp()) {
                result.put("token", response.getToken());
                User user = authService.findByEmail(request.getEmail());
                result.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole().toString()
                ));
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur login: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Email ou mot de passe incorrect"
            ));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody AuthDtos.VerifyOtpRequest request) {
        log.info("Vérification OTP pour: {}", request.getEmail());
        try {
            String token = authService.verifyOtp(request.getEmail(), request.getOtp());
            User user = authService.findByEmail(request.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName(),
                    "role", user.getRole().toString()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("Demande de reset password pour: {}", email);
        
        try {
            boolean sent = passwordResetService.requestPasswordReset(email);
            if (sent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email de réinitialisation envoyé"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Erreur lors de l'envoi de l'email"
                ));
            }
        } catch (Exception e) {
            log.error("Erreur forgot password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");
        
        try {
            boolean success = passwordResetService.resetPassword(token, newPassword);
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mot de passe réinitialisé avec succès"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token invalide ou expiré"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}