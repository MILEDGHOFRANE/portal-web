package com.dxc.sla;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.dxc.sla.repository")  // ← AJOUTE CETTE LIGNE !
public class SlaMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SlaMicroserviceApplication.class, args);
    }
}