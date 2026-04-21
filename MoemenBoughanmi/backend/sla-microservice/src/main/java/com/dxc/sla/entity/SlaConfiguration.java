package com.dxc.sla.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_configurations")
@Data
public class SlaConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String account;

    @Column(nullable = false)
    private String deskAccount;

    @Column(nullable = false)
    private String timeFrame;

    @Column(nullable = false)
    private String timeFrameOoh;

    private String timeFrameOther;

    @Column(nullable = false)
    private String answerSla;

    @Column(nullable = false)
    private String abandonSla;

    private String otherSla;

    @Column(nullable = false)
    private String targetAnswerRate;

    @Column(nullable = false)
    private String targetAbandonRate;

    private String targetOther;

    @Column(nullable = false)
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
}
