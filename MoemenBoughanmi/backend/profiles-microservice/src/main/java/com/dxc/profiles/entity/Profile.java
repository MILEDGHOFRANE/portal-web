package com.dxc.profiles.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId; // Lien avec auth_users (id)

    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Personal Info
    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 50)
    private String nationalId; // ID: 07039118

    @Column(length = 255)
    private String residentialAddress; // 05, Avenue De Paris Boumhel

    @Column(length = 20)
    private String contactPhone; // 98603014

    @Column(length = 100)
    private String familyStatus; // Married (1 Kids)

    private LocalDate dateOfBirth;

    @Column(length = 50)
    private String nationality;

    // Career & Desk
    @Column(length = 100)
    private String professionalTitle; // CTO

    @Column(length = 100)
    private String currentShift; // Not set

    @Column(length = 150)
    private String corporateEmail; // mehdi.khamlia2@dxc.com

    @Column(length = 100)
    private String department;

    @Column(length = 200)
    private String assignedProject;

    private LocalDate hireDate;

    @Column(length = 100)
    private String employeeNumber;

    // Technical Skills (JSON array stored as string)
    @Column(length = 1000)
    private String technicalSkills; // ["sys admin", "new dep"]

    // Hierarchy
    @Column(length = 50)
    private String role; // Admin

    @Column(length = 150)
    private String managerEmail; // Mehdi.khamlia2@dxc.com

    @Column(length = 150)
    private String hrManagerEmail; // ben-rabia@dxc.com

    // Profile Picture
    @Column(length = 500)
    private String profilePictureUrl;

    // Status
    @Column(nullable = false)
    private Boolean isActive = true;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
