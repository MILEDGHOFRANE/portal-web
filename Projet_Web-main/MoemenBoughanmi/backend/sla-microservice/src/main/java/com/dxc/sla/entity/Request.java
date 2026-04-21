package com.dxc.sla.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
public class Request {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String requestNumber; // REQ-2024-001
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(nullable = false, length = 1000)
    private String motif; // Raison de la demande
    
    @Column(nullable = false)
    private LocalDateTime requestDate;
    
    // Pour les congés
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;
    
    // Approbation
    private String approvedBy; // HR ou MANAGER
    private Long approvedByUserId;
    private LocalDateTime approvedDate;
    
    // Rejet
    private String rejectedBy;
    private Long rejectedByUserId;
    private LocalDateTime rejectedDate;
    private String rejectionReason;
    
    // Transport (pour abonnement transport)
    private String transportType;    // BUS ou TAXI
    private String pickupAddress;    // Adresse de départ (domicile)

    // Document généré (pour attestations)
    private String documentPath;
    private Boolean documentGenerated = false;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        requestDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
