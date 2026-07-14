package com.dms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    private Boolean inAppEnabled = true;

    @Column(name = "approval_required_email")
    private Boolean approvalRequiredEmail = true;

    @Column(name = "approval_required_in_app")
    private Boolean approvalRequiredInApp = true;

    @Column(name = "approved_email")
    private Boolean approvedEmail = true;

    @Column(name = "approved_in_app")
    private Boolean approvedInApp = true;

    @Column(name = "rejected_email")
    private Boolean rejectedEmail = true;

    @Column(name = "rejected_in_app")
    private Boolean rejectedInApp = true;

    @Column(name = "commented_email")
    private Boolean commentedEmail = true;

    @Column(name = "commented_in_app")
    private Boolean commentedInApp = true;

    @Column(name = "escalated_email")
    private Boolean escalatedEmail = true;

    @Column(name = "escalated_in_app")
    private Boolean escalatedInApp = true;
}