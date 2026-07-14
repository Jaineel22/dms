package com.dms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Stores raw foreign-key IDs (userId, entityId) rather than JPA relationships,
 * so audit history survives even after the referenced records are deleted.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static final class EntityType {
        public static final String USER = "USER";
        public static final String DEPARTMENT = "DEPARTMENT";
        public static final String DOCUMENT = "DOCUMENT";
        public static final String DOCUMENT_VERSION = "DOCUMENT_VERSION";
        public static final String WORKFLOW_DEFINITION = "WORKFLOW_DEFINITION";
        public static final String WORKFLOW_INSTANCE = "WORKFLOW_INSTANCE";
        public static final String APPROVAL = "APPROVAL";
        public static final String COMMENT = "COMMENT";
        public static final String HIERARCHY = "HIERARCHY";
        public static final String NOTIFICATION = "NOTIFICATION";

        private EntityType() {
        }
    }

    public static final class Action {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String VIEW = "VIEW";
        public static final String UPLOAD = "UPLOAD";
        public static final String DOWNLOAD = "DOWNLOAD";
        public static final String SUBMIT = "SUBMIT";
        public static final String APPROVE = "APPROVE";
        public static final String REJECT = "REJECT";
        public static final String SEND_BACK = "SEND_BACK";
        public static final String ESCALATE = "ESCALATE";
        public static final String ARCHIVE = "ARCHIVE";
        public static final String RESTORE = "RESTORE";
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
        public static final String RESET_PASSWORD = "RESET_PASSWORD";
        public static final String ASSIGN_MANAGER = "ASSIGN_MANAGER";
        public static final String UPDATE_MANAGER = "UPDATE_MANAGER";

        private Action() {
        }
    }
}