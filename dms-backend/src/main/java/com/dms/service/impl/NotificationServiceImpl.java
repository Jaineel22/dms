package com.dms.service.impl;

import com.dms.dto.request.NotificationPreferencesRequest;
import com.dms.dto.request.NotificationRequest;
import com.dms.dto.response.NotificationPreferenceResponse;
import com.dms.dto.response.NotificationResponse;
import com.dms.dto.response.NotificationSummaryResponse;
import com.dms.entity.Notification;
import com.dms.entity.NotificationPreference;
import com.dms.entity.User;
import com.dms.exception.ResourceNotFoundException;
import com.dms.exception.WorkflowException;
import com.dms.mapper.NotificationMapper;
import com.dms.repository.NotificationPreferenceRepository;
import com.dms.repository.NotificationRepository;
import com.dms.repository.UserRepository;
import com.dms.util.SecurityUtils;
import com.dms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setPriority(request.getPriority() != null ? request.getPriority() : Notification.Priority.MEDIUM);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setLink(request.getLink());
        notification.setIsRead(false);
        notification.setIsEmailSent(false);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification {} created for user {} (type={})", saved.getId(), user.getId(), request.getType());
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NotificationResponse createNotificationForUser(Long userId, String type, String title, String message, String link) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        NotificationPreference preference = getOrCreateDefaultPreference(userId);
        boolean inAppWanted = Boolean.TRUE.equals(preference.getInAppEnabled()) && isInAppEnabledForType(preference, type);
        boolean emailWanted = Boolean.TRUE.equals(preference.getEmailEnabled()) && isEmailEnabledForType(preference, type);

        if (!inAppWanted && !emailWanted) {
            log.debug("Skipping notification of type {} for user {} due to preferences", type, userId);
            return null;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setPriority(defaultPriorityForType(type));
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setIsRead(false);
        // Persisted regardless of channel; the pending-email job re-checks preferences before actually sending.
        notification.setIsEmailSent(!emailWanted);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification {} created for user {} (type={})", saved.getId(), userId, type);
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void createBulkNotifications(List<Long> userIds, String type, String title, String message, String link) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            try {
                createNotificationForUser(userId, type, title, message, link);
            } catch (Exception e) {
                log.warn("Failed to create notification for user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        validateOwner(notification);

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
        log.info("All notifications marked as read for user {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getAllNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationSummaryResponse getNotificationSummary(Long userId) {
        long totalUnread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);

        Map<String, Long> byPriority = unread.stream()
                .collect(Collectors.groupingBy(Notification::getPriority, Collectors.counting()));
        Map<String, Long> byType = unread.stream()
                .collect(Collectors.groupingBy(Notification::getType, Collectors.counting()));

        List<Notification> recent = notificationRepository
                .findByUserId(userId, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();

        return notificationMapper.toNotificationSummaryResponse(totalUnread, byPriority, byType, recent);
    }

    @Override
    @Transactional
    public void sendPendingEmailNotifications() {
        List<Notification> pending = notificationRepository
                .findByIsEmailSentFalseAndCreatedAtBefore(LocalDateTime.now());

        for (Notification notification : pending) {
            try {
                NotificationPreference preference = getOrCreateDefaultPreference(notification.getUser().getId());
                if (!Boolean.TRUE.equals(preference.getEmailEnabled())
                        || !isEmailEnabledForType(preference, notification.getType())) {
                    // Preference changed since creation; stop tracking this as pending.
                    notification.setIsEmailSent(true);
                    notificationRepository.save(notification);
                    continue;
                }

                // TODO: integrate with the actual email delivery mechanism (e.g. Resend/SMTP).
                log.info("Sending email notification {} to user {}", notification.getId(), notification.getUser().getId());

                notification.setIsEmailSent(true);
                notification.setEmailSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
            } catch (Exception e) {
                log.error("Failed to send email notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }

    // Not readOnly: getOrCreateDefaultPreference() may insert a default row on first access.
    @Override
    @Transactional
    public NotificationPreferenceResponse getPreferences(Long userId) {
        return toPreferenceResponse(getOrCreateDefaultPreference(userId));
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updatePreferences(Long userId, NotificationPreferencesRequest request) {
        NotificationPreference preference = getOrCreateDefaultPreference(userId);

        applyIfPresent(request.getEmailEnabled(), preference::setEmailEnabled);
        applyIfPresent(request.getInAppEnabled(), preference::setInAppEnabled);
        applyIfPresent(request.getApprovalRequiredEmail(), preference::setApprovalRequiredEmail);
        applyIfPresent(request.getApprovalRequiredInApp(), preference::setApprovalRequiredInApp);
        applyIfPresent(request.getApprovedEmail(), preference::setApprovedEmail);
        applyIfPresent(request.getApprovedInApp(), preference::setApprovedInApp);
        applyIfPresent(request.getRejectedEmail(), preference::setRejectedEmail);
        applyIfPresent(request.getRejectedInApp(), preference::setRejectedInApp);
        applyIfPresent(request.getCommentedEmail(), preference::setCommentedEmail);
        applyIfPresent(request.getCommentedInApp(), preference::setCommentedInApp);
        applyIfPresent(request.getEscalatedEmail(), preference::setEscalatedEmail);
        applyIfPresent(request.getEscalatedInApp(), preference::setEscalatedInApp);

        NotificationPreference saved = notificationPreferenceRepository.save(preference);
        log.info("Notification preferences updated for user {}", userId);
        return toPreferenceResponse(saved);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        validateOwner(notification);
        notificationRepository.delete(notification);
    }

    private void validateOwner(Notification notification) {
        Long currentUserId = securityUtils.getCurrentUserId();
        boolean isOwner = notification.getUser() != null && notification.getUser().getId().equals(currentUserId);
        boolean isAdmin = securityUtils.hasRole("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new WorkflowException("You are not authorized to modify this notification");
        }
    }

    private NotificationPreference getOrCreateDefaultPreference(Long userId) {
        return notificationPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    NotificationPreference preference = new NotificationPreference();
                    preference.setUser(user);
                    preference.setEmailEnabled(true);
                    preference.setInAppEnabled(true);
                    preference.setApprovalRequiredEmail(true);
                    preference.setApprovalRequiredInApp(true);
                    preference.setApprovedEmail(true);
                    preference.setApprovedInApp(true);
                    preference.setRejectedEmail(true);
                    preference.setRejectedInApp(true);
                    preference.setCommentedEmail(true);
                    preference.setCommentedInApp(true);
                    preference.setEscalatedEmail(true);
                    preference.setEscalatedInApp(true);
                    return notificationPreferenceRepository.save(preference);
                });
    }

    private boolean isInAppEnabledForType(NotificationPreference preference, String type) {
        return switch (type) {
            case Notification.Type.APPROVAL_REQUIRED -> Boolean.TRUE.equals(preference.getApprovalRequiredInApp());
            case Notification.Type.APPROVED -> Boolean.TRUE.equals(preference.getApprovedInApp());
            case Notification.Type.REJECTED, Notification.Type.SENT_BACK -> Boolean.TRUE.equals(preference.getRejectedInApp());
            case Notification.Type.COMMENT_ADDED -> Boolean.TRUE.equals(preference.getCommentedInApp());
            case Notification.Type.ESCALATED -> Boolean.TRUE.equals(preference.getEscalatedInApp());
            default -> true; // DOCUMENT_UPLOADED, DOCUMENT_SUBMITTED, REMINDER, etc. — no dedicated toggle
        };
    }

    private boolean isEmailEnabledForType(NotificationPreference preference, String type) {
        return switch (type) {
            case Notification.Type.APPROVAL_REQUIRED -> Boolean.TRUE.equals(preference.getApprovalRequiredEmail());
            case Notification.Type.APPROVED -> Boolean.TRUE.equals(preference.getApprovedEmail());
            case Notification.Type.REJECTED, Notification.Type.SENT_BACK -> Boolean.TRUE.equals(preference.getRejectedEmail());
            case Notification.Type.COMMENT_ADDED -> Boolean.TRUE.equals(preference.getCommentedEmail());
            case Notification.Type.ESCALATED -> Boolean.TRUE.equals(preference.getEscalatedEmail());
            default -> true;
        };
    }

    private String defaultPriorityForType(String type) {
        return switch (type) {
            case Notification.Type.ESCALATED, Notification.Type.APPROVAL_REQUIRED -> Notification.Priority.HIGH;
            case Notification.Type.REMINDER -> Notification.Priority.LOW;
            default -> Notification.Priority.MEDIUM;
        };
    }

    private void applyIfPresent(Boolean value, Consumer<Boolean> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private NotificationPreferenceResponse toPreferenceResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .userId(preference.getUser().getId())
                .emailEnabled(preference.getEmailEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .approvalRequiredEmail(preference.getApprovalRequiredEmail())
                .approvalRequiredInApp(preference.getApprovalRequiredInApp())
                .approvedEmail(preference.getApprovedEmail())
                .approvedInApp(preference.getApprovedInApp())
                .rejectedEmail(preference.getRejectedEmail())
                .rejectedInApp(preference.getRejectedInApp())
                .commentedEmail(preference.getCommentedEmail())
                .commentedInApp(preference.getCommentedInApp())
                .escalatedEmail(preference.getEscalatedEmail())
                .escalatedInApp(preference.getEscalatedInApp())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}