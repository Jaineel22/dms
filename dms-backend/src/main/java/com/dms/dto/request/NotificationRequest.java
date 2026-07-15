package com.dms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NOTE: not part of the Phase 5(b) DTO list, but required as the parameter type for
 * NotificationService.createNotification(NotificationRequest) — that method was specified
 * on the interface without a matching request DTO, so this was added to make it compile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Type is required")
    private String type;

    /** Defaults to MEDIUM if not provided. */
    private String priority;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String link;
}