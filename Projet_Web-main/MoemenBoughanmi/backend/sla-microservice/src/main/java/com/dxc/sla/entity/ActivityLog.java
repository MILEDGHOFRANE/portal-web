package com.dxc.sla.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String userFullName;
    private String action;

    @Column(length = 500)
    private String description;

    private String entityType;
    private Long entityId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
