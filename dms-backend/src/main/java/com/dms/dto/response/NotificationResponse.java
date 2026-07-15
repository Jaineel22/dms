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
public class NotificationResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String type;
    private String priority;
    private String title;
    private String message;
    private String link;
    private Boolean isRead;
    private Boolean isEmailSent;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}