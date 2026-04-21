package com.dxc.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthMicroserviceApplication.class, args);
        
        System.out.println("\n" +
            "╔═══════════════════════════════════════════════════════════╗\n" +
            "║                                                           ║\n" +
            "║   DXC AUTHENTICATION MICROSERVICE STARTED SUCCESSFULLY!   ║\n" +
            "║                                                           ║\n" +
            "║   Port: 8083                                              ║\n" +
            "║   API:  http://localhost:8083/api/auth                    ║\n" +
            "║   H2 Console: http://localhost:8083/h2-console            ║\n" +
            "║                                                           ║\n" +
            "║   Endpoints:                                              ║\n" +
            "║   - POST /api/auth/register       (Inscription)           ║\n" +
            "║   - GET  /api/auth/verify?token=  (Vérification email)    ║\n" +
            "║   - POST /api/auth/login          (Connexion)             ║\n" +
            "║   - POST /api/auth/verify-otp     (Vérification OTP)      ║\n" +
            "║   - GET  /api/auth/health         (Health check)          ║\n" +
            "║                                                           ║\n" +
            "╚═══════════════════════════════════════════════════════════╝\n");
    }
}
