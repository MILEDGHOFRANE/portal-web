package com.dxc.sla.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String employeeId; // EMP-2024-001
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String position; // Poste
    
    @Column(nullable = false)
    private String department; // Département
    
    @Column(nullable = false)
    private LocalDate hireDate; // Date d'embauche
    
    @Column(nullable = false)
    private Double salary; // Salaire mensuel
    
    private String phone;
    
    private String address;
    
    // Congés
    @Column(nullable = false)
    private Integer totalLeaveDays = 30; // Total jours congés par an
    
    @Column(nullable = false)
    private Integer usedLeaveDays = 0; // Jours utilisés
    
    @Column(nullable = false)
    private Integer remainingLeaveDays = 30; // Jours restants
    
    // Lien avec le user auth
    private Long userId;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Méthode utilitaire : temps de travail
    public String getWorkDuration() {
        if (hireDate == null) return "N/A";
        
        LocalDate now = LocalDate.now();
        long years = java.time.temporal.ChronoUnit.YEARS.between(hireDate, now);
        long months = java.time.temporal.ChronoUnit.MONTHS.between(hireDate.plusYears(years), now);
        
        if (years > 0) {
            return years + " an" + (years > 1 ? "s" : "") + " " + months + " mois";
        } else {
            return months + " mois";
        }
    }
}
