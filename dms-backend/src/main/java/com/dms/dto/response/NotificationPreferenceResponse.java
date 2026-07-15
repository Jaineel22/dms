package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private Long userId;
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
    private LocalDateTime updatedAt;
}