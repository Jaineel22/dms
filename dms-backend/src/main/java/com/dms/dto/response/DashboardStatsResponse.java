package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // User Stats
    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;

    // Document Stats
    private Long totalDocuments;
    private Long draftCount;
    private Long underReviewCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long archivedCount;

    // Workflow Stats
    private Long totalWorkflows;
    private Long pendingApprovals;
    private Long inProgressWorkflows;
    private Long approvedWorkflows;
    private Long rejectedWorkflows;
    private Long expiredWorkflows;

    // Storage
    private Long totalStorageBytes;

    /** Human-readable form of totalStorageBytes, e.g. "128.4 MB". */
    private String totalStorageReadable;

    // Activity
    private List<ActivityResponse> recentActivities;
}