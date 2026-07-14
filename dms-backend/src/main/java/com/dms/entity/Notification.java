package com.dms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "MEDIUM";

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "link", length = 500)
    private String link;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_email_sent", nullable = false)
    private Boolean isEmailSent = false;

    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public static final class Type {
        public static final String APPROVAL_REQUIRED = "APPROVAL_REQUIRED";
        public static final String APPROVED = "APPROVED";
        public static final String REJECTED = "REJECTED";
        public static final String SENT_BACK = "SENT_BACK";
        public static final String ESCALATED = "ESCALATED";
        public static final String COMMENT_ADDED = "COMMENT_ADDED";
        public static final String DOCUMENT_UPLOADED = "DOCUMENT_UPLOADED";
        public static final String DOCUMENT_SUBMITTED = "DOCUMENT_SUBMITTED";
        public static final String REMINDER = "REMINDER";

        private Type() {
        }
    }

    public static final class Priority {
        public static final String HIGH = "HIGH";
        public static final String MEDIUM = "MEDIUM";
        public static final String LOW = "LOW";

        private Priority() {
        }
    }
}