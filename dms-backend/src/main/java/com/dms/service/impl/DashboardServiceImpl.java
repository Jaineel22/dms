package com.dms.service.impl;

import com.dms.dto.response.ActivityResponse;
import com.dms.dto.response.DashboardStatsResponse;
import com.dms.entity.AuditLog;
import com.dms.entity.WorkflowInstance.WorkflowInstanceStatus;
import com.dms.mapper.DashboardMapper;
import com.dms.repository.AuditLogRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.UserRepository;
import com.dms.repository.WorkflowInstanceRepository;
import com.dms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Relies on a handful of repository methods that don't yet exist on DocumentRepository /
 * UserRepository from earlier phases (countByWorkflowStatus, countByIsArchivedTrue, sumFileSize,
 * countByIsActiveTrue). See patches/RepositoryAdditions.patch.java for the exact methods to add.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int DEFAULT_RECENT_ACTIVITY_LIMIT = 10;

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AuditLogRepository auditLogRepository;
    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        Map<String, Long> userStats = getUserStats();
        Map<String, Long> docStats = getDocumentStats();
        Map<String, Long> workflowStats = getWorkflowStats();
        long totalStorageBytes = documentRepository.sumFileSize();
        List<ActivityResponse> recentActivities = getRecentActivities(DEFAULT_RECENT_ACTIVITY_LIMIT);

        return dashboardMapper.toDashboardStatsResponse(
                userStats.get("total"), userStats.get("active"), userStats.get("inactive"),
                docStats.get("total"), docStats.get("draft"), docStats.get("underReview"),
                docStats.get("approved"), docStats.get("rejected"), docStats.get("archived"),
                workflowStats.get("total"), workflowStats.get("pending"), workflowStats.get("inProgress"),
                workflowStats.get("approved"), workflowStats.get("rejected"), workflowStats.get("expired"),
                totalStorageBytes, recentActivities);
    }

    @Override
    public DashboardStatsResponse getUserDashboardStats(Long userId) {
        long totalDocuments = documentRepository.countByOwnerId(userId);
        long draft = documentRepository.countByOwnerIdAndWorkflowStatus(userId, "DRAFT");
        long underReview = documentRepository.countByOwnerIdAndWorkflowStatus(userId, "UNDER_REVIEW");
        long approved = documentRepository.countByOwnerIdAndWorkflowStatus(userId, "APPROVED");
        long rejected = documentRepository.countByOwnerIdAndWorkflowStatus(userId, "REJECTED");
        long archived = documentRepository.countByOwnerIdAndIsArchivedTrue(userId);

        long totalWorkflows = workflowInstanceRepository.findBySubmittedById(userId, Pageable.unpaged()).getTotalElements();
        long pending = countBySubmitterAndStatus(userId, WorkflowInstanceStatus.PENDING);
        long inProgress = countBySubmitterAndStatus(userId, WorkflowInstanceStatus.IN_PROGRESS);
        long approvedWf = countBySubmitterAndStatus(userId, WorkflowInstanceStatus.APPROVED);
        long rejectedWf = countBySubmitterAndStatus(userId, WorkflowInstanceStatus.REJECTED);
        long expiredWf = countBySubmitterAndStatus(userId, WorkflowInstanceStatus.EXPIRED);

        List<AuditLog> userLogs = auditLogRepository
                .findByUserId(userId, PageRequest.of(0, DEFAULT_RECENT_ACTIVITY_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
        List<ActivityResponse> recentActivities = enrichActivities(userLogs);

        return dashboardMapper.toDashboardStatsResponse(
                0, 0, 0, // totalUsers/activeUsers/inactiveUsers are not meaningful for a single-user view
                totalDocuments, draft, underReview, approved, rejected, archived,
                totalWorkflows, pending, inProgress, approvedWf, rejectedWf, expiredWf,
                documentRepository.sumFileSizeByOwnerId(userId), recentActivities);
    }

    @Override
    public List<ActivityResponse> getRecentActivities(int limit) {
        List<AuditLog> logs = auditLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit))
                .getContent();
        return enrichActivities(logs);
    }

    @Override
    public Map<String, Long> getDocumentStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", documentRepository.count());
        stats.put("draft", documentRepository.countByWorkflowStatus("DRAFT"));
        stats.put("underReview", documentRepository.countByWorkflowStatus("UNDER_REVIEW"));
        stats.put("approved", documentRepository.countByWorkflowStatus("APPROVED"));
        stats.put("rejected", documentRepository.countByWorkflowStatus("REJECTED"));
        stats.put("archived", documentRepository.countByIsArchivedTrue());
        return stats;
    }

    @Override
    public Map<String, Long> getWorkflowStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", workflowInstanceRepository.count());
        stats.put("pending", countByStatus(WorkflowInstanceStatus.PENDING));
        stats.put("inProgress", countByStatus(WorkflowInstanceStatus.IN_PROGRESS));
        stats.put("approved", countByStatus(WorkflowInstanceStatus.APPROVED));
        stats.put("rejected", countByStatus(WorkflowInstanceStatus.REJECTED));
        stats.put("expired", countByStatus(WorkflowInstanceStatus.EXPIRED));
        return stats;
    }

    @Override
    public Map<String, Long> getUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByIsActiveTrue();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("inactive", total - active);
        return stats;
    }

    private long countByStatus(WorkflowInstanceStatus status) {
        return workflowInstanceRepository.findByStatus(status, Pageable.unpaged()).getTotalElements();
    }

    private long countBySubmitterAndStatus(Long userId, WorkflowInstanceStatus status) {
        return workflowInstanceRepository.findBySubmittedById(userId, Pageable.unpaged()).getContent().stream()
                .filter(wi -> wi.getStatus() == status)
                .count();
    }

    private List<ActivityResponse> enrichActivities(List<AuditLog> logs) {
        List<ActivityResponse> responses = dashboardMapper.toActivityResponseList(logs);
        for (int i = 0; i < logs.size(); i++) {
            AuditLog log = logs.get(i);
            ActivityResponse response = responses.get(i);
            userRepository.findById(log.getUserId()).ifPresentOrElse(
                    user -> {
                        response.setUserFullName(user.getFullName());
                        response.setUserEmail(user.getEmail());
                    },
                    () -> response.setUserFullName("Unknown User")
            );
            // entityName resolution is intentionally left to callers that know the entity type
            // (e.g. document title lookups) rather than guessed here across every entity type.
        }
        return responses;
    }
}