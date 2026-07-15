package com.dms.service;

import com.dms.dto.request.NotificationPreferencesRequest;
import com.dms.dto.request.NotificationRequest;
import com.dms.dto.response.NotificationPreferenceResponse;
import com.dms.dto.response.NotificationResponse;
import com.dms.dto.response.NotificationSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse createNotificationForUser(Long userId, String type, String title, String message, String link);

    void createBulkNotifications(List<Long> userIds, String type, String title, String message, String link);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable);

    Page<NotificationResponse> getAllNotifications(Long userId, Pageable pageable);

    NotificationSummaryResponse getNotificationSummary(Long userId);

    void sendPendingEmailNotifications();

    NotificationPreferenceResponse getPreferences(Long userId);

    NotificationPreferenceResponse updatePreferences(Long userId, NotificationPreferencesRequest request);

    void deleteNotification(Long notificationId);
}