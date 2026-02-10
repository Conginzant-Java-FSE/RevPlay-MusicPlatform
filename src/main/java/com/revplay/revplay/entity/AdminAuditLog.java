package com.revplay.revplay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User adminUser;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "target_entity", nullable = false)
    private String targetEntity;

    @Column(name = "target_entity_id", nullable = false)
    private String targetEntityId;

    private String details;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

