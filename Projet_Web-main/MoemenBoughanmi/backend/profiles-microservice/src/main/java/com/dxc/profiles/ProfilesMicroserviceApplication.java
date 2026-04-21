package com.dxc.profiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProfilesMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProfilesMicroserviceApplication.class, args);
        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   DXC PROFILES MICROSERVICE STARTED SUCCESSFULLY!         ║");
        System.out.println("║   Port: 8084                                              ║");
        System.out.println("║   API:  http://localhost:8084/api/profiles                ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");
    }
}
