package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSummaryResponse {

    private Long totalUnreadCount;

    /** Keyed by priority: HIGH, MEDIUM, LOW. */
    private Map<String, Long> unreadByPriority;

    /** Keyed by notification type, e.g. APPROVAL_REQUIRED, COMMENT_ADDED, etc. */
    private Map<String, Long> unreadByType;

    /** Most recent notifications for the user, capped at 5. */
    private List<NotificationResponse> recentNotifications;
}