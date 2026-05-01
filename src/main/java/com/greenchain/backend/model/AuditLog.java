package com.greenchain.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String ipAddress;

    @Column(length = 500)
    private String description;

    public enum Action {
        CREATE,
        UPDATE,
        DELETE,
        VIEW,
        EXPORT
    }

    public enum EntityType {
        SUPPLIER,
        SHIPMENT,
        USER,
        TRANSPORT_MODE
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public AuditLog() {
    }

    public AuditLog(String username, Action action, EntityType entityType, Long entityId) {
        this.username = username;
        this.action = action.name();
        this.entityType = entityType.name();
        this.entityId = entityId;
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String username, Action action, EntityType entityType, Long entityId, String description) {
        this(username, action, entityType, entityId);
        this.description = description;
    }

    public AuditLog(String username, Action action, EntityType entityType, Long entityId,
                    String oldValue, String newValue, String description) {
        this(username, action, entityType, entityId, description);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
