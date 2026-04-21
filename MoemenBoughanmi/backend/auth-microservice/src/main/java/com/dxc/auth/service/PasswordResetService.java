package com.dxc.auth.service;

import com.dxc.auth.entity.PasswordReset;
import com.dxc.auth.entity.User;
import com.dxc.auth.repository.PasswordResetRepository;
import com.dxc.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
@Transactional
public class PasswordResetService {

    @Autowired
    private PasswordResetRepository resetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Request a password reset for the given email.
     * @param email User's email
     * @return true if email sent successfully, false otherwise
     */
    public boolean requestPasswordReset(String email) {
        log.info("Demande de réinitialisation pour: {}", email);

        // Retrieve user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Delete any previous reset requests
        resetRepository.deleteByUserEmail(email);

        // Generate secure token and selector
        String resetToken = generateSecureToken();
        String resetSelector = generateSecureToken();

        // Create PasswordReset entity
        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUserId(user.getId());
        passwordReset.setUserEmail(email);
        passwordReset.setResetToken(resetToken);
        passwordReset.setResetSelector(resetSelector);

        // Save to repository
        resetRepository.save(passwordReset);

        // Send reset email
        try {
            emailService.sendPasswordResetEmail(email ,  user.getFirstName() , resetToken);
        } catch (Exception e) {
            log.error("Erreur envoi email reset: {}", e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Reset the password using a valid token.
     * @param token The reset token
     * @param newPassword The new password
     * @return true if reset successful
     */
    public boolean resetPassword(String token, String newPassword) {
        log.info("Tentative de reset password avec token");

        // Find password reset request
        PasswordReset passwordReset = resetRepository.findByResetTokenAndUsedFalse(token)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        // Check if token is expired
        if (passwordReset.isExpired()) {
            throw new RuntimeException("Le token a expiré");
        }

        // Retrieve user
        User user = userRepository.findById(passwordReset.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        passwordReset.setUsed(true);
        resetRepository.save(passwordReset);

        log.info("Mot de passe réinitialisé pour: {}", user.getEmail());
        return true;
    }

    /**
     * Generate a secure random token.
     * @return Base64-encoded token
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}