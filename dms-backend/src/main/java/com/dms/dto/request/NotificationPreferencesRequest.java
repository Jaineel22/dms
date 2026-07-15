package com.dms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Patch-style update: only non-null fields are applied by the service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesRequest {

    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean approvalRequiredEmail;
    private Boolean approvalRequiredInApp;
    private Boolean approvedEmail;
    private Boolean approvedInApp;
    private Boolean rejectedEmail;
    private Boolean rejectedInApp;
    private Boolean commentedEmail;
    private Boolean commentedInApp;
    private Boolean escalatedEmail;
    private Boolean escalatedInApp;
}