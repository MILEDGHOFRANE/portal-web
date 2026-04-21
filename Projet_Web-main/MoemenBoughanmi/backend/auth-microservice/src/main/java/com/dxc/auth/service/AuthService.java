package com.dxc.auth.service;

import com.dxc.auth.dto.AuthDtos;
import com.dxc.auth.entity.OtpCode;
import com.dxc.auth.entity.User;
import com.dxc.auth.entity.VerificationToken;
import com.dxc.auth.repository.OtpCodeRepository;
import com.dxc.auth.repository.UserRepository;
import com.dxc.auth.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    public void register(AuthDtos.RegisterRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        String firstName = request.getFirstName();
        String lastName = request.getLastName();

        log.info("Tentative d'inscription pour: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(User.Role.EMPLOYEE);
        user.setIsVerified(false);
        user.setIsActive(true);

     
      log.info("Création du compte pour: {}", email);
    
userRepository.save(user);

// Générer le token de vérification
String verificationToken = java.util.UUID.randomUUID().toString();
VerificationToken vToken = new VerificationToken();
vToken.setToken(verificationToken);
vToken.setUser(user);
vToken.setExpiresAt(LocalDateTime.now().plusHours(24));
vToken.setUsed(false);
verificationTokenRepository.save(vToken);

try {
    emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
} catch (Exception e) {
    log.error("Erreur lors de l'envoi de l'email de vérification: {}", e.getMessage());
}
log.info("Email de vérification envoyé à: {}", email);
    };
    

    public void verifyEmailToken(String token) {
        log.info("Vérification du token email");

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (verificationToken.getUsed()) {
            throw new RuntimeException("Ce token a déjà été utilisé");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Ce token a expiré");
        }

        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Email vérifié avec succès pour: {}", user.getEmail());

try {
    emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
} catch (Exception e) {
    log.error("Erreur lors de l'envoi de l'email de bienvenue: {}", e.getMessage());
}
        log.info("Email de bienvenue envoyé à: {}", user.getEmail());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        log.info("Tentative de login pour: {}", email);
        log.info("Password provided: [PROTECTED], Length: {}", password.length());

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        log.info("User found in DB: {}, Stored Hash: {}", user.getEmail(), user.getPasswordHash());
        log.info("Password: {}", passwordEncoder.encode(password));

        if (!user.getIsActive()) {
            throw new RuntimeException("Ce compte a été désactivé");
        }

        if (user.getIsLocked()) {
            throw new RuntimeException("Ce compte a été verrouillé");
        }

        if (!user.getIsVerified()) {
            throw new RuntimeException("Veuillez vérifier votre email avant de vous connecter");
        }

        boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
        log.info("Password matches: {}", matches);

        if (!matches) {
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= 5) {
                user.setIsLocked(true);
                log.warn("Compte verrouillé après 5 tentatives: {}", email);
            } else {
                user.setFailedLoginAttempts(attempts);
            }
            userRepository.save(user);
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        // Si l'utilisateur a déjà vérifié son OTP une fois (lastLoginAt != null), on skip l'OTP
        if (user.getLastLoginAt() != null) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
            log.info("Connexion directe (OTP déjà vérifié) pour: {}", email);
            AuthDtos.AuthResponse directResponse = new AuthDtos.AuthResponse();
            directResponse.setSuccess(true);
            directResponse.setRequiresOtp(false);
            directResponse.setToken(token);
            directResponse.setMessage("Connexion réussie");
            return directResponse;
        }

        String otp = generateOtp();
        saveOtp(user, otp);
        try {
            emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'OTP: {}", e.getMessage());
        }
        log.info("OTP envoyé à: {}", email);

        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse();
        response.setSuccess(true);
        response.setRequiresOtp(true);
        response.setMessage("Code OTP envoyé par email");
        return response;
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void saveOtp(User user, String code) {
        OtpCode otpCode = new OtpCode();
        otpCode.setUser(user);
        otpCode.setCode(code);
        otpCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpCode.setUsed(false);
        otpCodeRepository.save(otpCode);
    }

    public String verifyOtp(String email, String otp) {
        log.info("Vérification OTP pour: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        OtpCode otpCode = otpCodeRepository.findByUserAndCodeAndUsedFalse(user, otp)
            .orElseThrow(() -> new RuntimeException("Code OTP invalide"));

        if (otpCode.isExpired()) {
            throw new RuntimeException("Ce code OTP a expiré");
        }

        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

    String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
        log.info("Connexion réussie pour: {}", email);

        return token;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}