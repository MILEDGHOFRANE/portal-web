package com.dxc.sla.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "configurations")
@Data
public class AccountConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String account;

    @Column(name = "desk_account", nullable = false, length = 100)
    private String deskAccount;

    @Column(name = "time_frame", nullable = false)
    private Integer timeFrame;

    @Column(name = "time_frame_ooh")
    private Integer timeFrameOoh;

    @Column(name = "time_frame_other", length = 50)
    private String timeFrameOther;

    @Column(name = "answer_sla", nullable = false, length = 50)
    private String answerSla;

    @Column(name = "abandon_sla", nullable = false, length = 50)
    private String abandonSla;

    @Column(name = "other_sla", length = 50)
    private String otherSla;

    @Column(name = "target_answer_rate", nullable = false, length = 50)
    private String targetAnswerRate;

    @Column(name = "target_abandon_rate", nullable = false, length = 50)
    private String targetAbandonRate;

    @Column(name = "target_other", length = 50)
    private String targetOther;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
