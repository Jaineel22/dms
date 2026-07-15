package com.dms.mapper;

import com.dms.dto.response.ActivityResponse;
import com.dms.dto.response.DashboardStatsResponse;
import com.dms.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * userFullName / userEmail / entityName on ActivityResponse are intentionally left unset
 * here for the same reason as AuditLogMapper: AuditLog only stores raw IDs. DashboardServiceImpl
 * enriches those fields after mapping.
 */
@Mapper(componentModel = "spring")
public interface DashboardMapper {

    @Mapping(target = "userFullName", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    @Mapping(target = "entityName", ignore = true)
    ActivityResponse toActivityResponse(AuditLog auditLog);

    List<ActivityResponse> toActivityResponseList(List<AuditLog> auditLogs);

    default DashboardStatsResponse toDashboardStatsResponse(
            long totalUsers, long activeUsers, long inactiveUsers,
            long totalDocuments, long draftCount, long underReviewCount,
            long approvedCount, long rejectedCount, long archivedCount,
            long totalWorkflows, long pendingApprovals, long inProgressWorkflows,
            long approvedWorkflows, long rejectedWorkflows, long expiredWorkflows,
            long totalStorageBytes, List<ActivityResponse> recentActivities) {

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .totalDocuments(totalDocuments)
                .draftCount(draftCount)
                .underReviewCount(underReviewCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .archivedCount(archivedCount)
                .totalWorkflows(totalWorkflows)
                .pendingApprovals(pendingApprovals)
                .inProgressWorkflows(inProgressWorkflows)
                .approvedWorkflows(approvedWorkflows)
                .rejectedWorkflows(rejectedWorkflows)
                .expiredWorkflows(expiredWorkflows)
                .totalStorageBytes(totalStorageBytes)
                .totalStorageReadable(formatBytes(totalStorageBytes))
                .recentActivities(recentActivities)
                .build();
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        unitIndex = Math.min(unitIndex, units.length - 1);
        double value = bytes / Math.pow(1024, unitIndex);
        return String.format("%.1f %s", value, units[unitIndex]);
    }
}