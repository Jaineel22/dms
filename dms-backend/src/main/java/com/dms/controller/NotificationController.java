package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.MarkNotificationsReadRequest;
import com.dms.dto.request.NotificationPreferencesRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.NotificationPreferenceResponse;
import com.dms.dto.response.NotificationResponse;
import com.dms.dto.response.NotificationSummaryResponse;
import com.dms.util.SecurityUtils;
import com.dms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
@Tag(name = "Notifications", description = "In-app notifications and notification preferences for the current user")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "Get all notifications for the current user")
    @GetMapping(ApiConstants.NOTIFICATIONS_BASE)
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getAllNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Page<NotificationResponse> response = notificationService.getAllNotifications(currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get unread notifications for the current user")
    @GetMapping(ApiConstants.NOTIFICATIONS_UNREAD)
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Page<NotificationResponse> response = notificationService.getUnreadNotifications(currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get unread notification counts by type/priority, plus recent notifications")
    @GetMapping(ApiConstants.NOTIFICATIONS_SUMMARY)
    public ResponseEntity<ApiResponse<NotificationSummaryResponse>> getNotificationSummary() {
        Long currentUserId = securityUtils.getCurrentUserId();
        NotificationSummaryResponse response = notificationService.getNotificationSummary(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Mark a single notification as read")
    @PutMapping(ApiConstants.NOTIFICATION_READ)
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @Operation(summary = "Mark notifications as read. With no body (or an empty id list), marks all "
            + "of the current user's unread notifications; with a populated id list, marks just those.")
    @PutMapping(ApiConstants.NOTIFICATIONS_READ_ALL)
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestBody(required = false) MarkNotificationsReadRequest request) {
        if (request != null && request.getNotificationIds() != null && !request.getNotificationIds().isEmpty()) {
            request.getNotificationIds().forEach(notificationService::markAsRead);
        } else {
            Long currentUserId = securityUtils.getCurrentUserId();
            notificationService.markAllAsRead(currentUserId);
        }
        return ResponseEntity.ok(ApiResponse.success("Notifications marked as read"));
    }

    @Operation(summary = "Delete a notification")
    @DeleteMapping(ApiConstants.NOTIFICATION_BY_ID)
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }

    @Operation(summary = "Get the current user's notification preferences")
    @GetMapping(ApiConstants.NOTIFICATIONS_PREFERENCES)
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences() {
        Long currentUserId = securityUtils.getCurrentUserId();
        NotificationPreferenceResponse response = notificationService.getPreferences(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update the current user's notification preferences (partial patch)")
    @PutMapping(ApiConstants.NOTIFICATIONS_PREFERENCES)
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @Valid @RequestBody NotificationPreferencesRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        NotificationPreferenceResponse response = notificationService.updatePreferences(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated", response));
    }
}