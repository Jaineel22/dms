package com.dms.scheduler;

import com.dms.entity.Notification;
import com.dms.entity.WorkflowInstance;
import com.dms.entity.WorkflowInstance.WorkflowInstanceStatus;
import com.dms.repository.WorkflowInstanceRepository;
import com.dms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NOTE: requires @EnableScheduling on your main application class (or a
 * dedicated @Configuration class) for these @Scheduled methods to actually run.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final WorkflowInstanceRepository workflowInstanceRepository;

    /** Daily at 9 AM: flush any notifications still waiting to be emailed. */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendPendingEmailNotifications() {
        log.info("Running scheduled job: sendPendingEmailNotifications");
        try {
            notificationService.sendPendingEmailNotifications();
        } catch (Exception e) {
            log.error("sendPendingEmailNotifications job failed: {}", e.getMessage(), e);
        }
    }

    /** Daily at 9 AM: remind approvers about workflow instances that are still awaiting action. */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendApprovalReminders() {
        log.info("Running scheduled job: sendApprovalReminders");
        try {
            List<WorkflowInstance> pending = workflowInstanceRepository
                    .findByStatus(WorkflowInstanceStatus.PENDING, Pageable.unpaged())
                    .getContent();
            List<WorkflowInstance> inProgress = workflowInstanceRepository
                    .findByStatus(WorkflowInstanceStatus.IN_PROGRESS, Pageable.unpaged())
                    .getContent();

            for (WorkflowInstance instance : pending) {
                remindIfApplicable(instance);
            }
            for (WorkflowInstance instance : inProgress) {
                remindIfApplicable(instance);
            }
        } catch (Exception e) {
            log.error("sendApprovalReminders job failed: {}", e.getMessage(), e);
        }
    }

    private void remindIfApplicable(WorkflowInstance instance) {
        if (instance.getCurrentApprover() == null) {
            return;
        }
        // Only remind once the instance is at or past its deadline, or has no deadline set
        // (in which case we still nudge daily rather than never).
        if (instance.getDeadlineAt() != null && instance.getDeadlineAt().isAfter(LocalDateTime.now())) {
            return;
        }

        String title = "Approval reminder";
        String message = "A document is still awaiting your approval and may be overdue.";
        String link = "/workflow/instances/" + instance.getId();

        notificationService.createNotificationForUser(
                instance.getCurrentApprover().getId(),
                Notification.Type.REMINDER,
                title,
                message,
                link);
    }
}