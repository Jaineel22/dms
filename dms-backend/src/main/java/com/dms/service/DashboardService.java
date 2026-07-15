package com.dms.service;

import com.dms.dto.response.ActivityResponse;
import com.dms.dto.response.DashboardStatsResponse;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardStatsResponse getDashboardStats();

    /** Same shape as getDashboardStats() but scoped to a single user's documents/workflows. */
    DashboardStatsResponse getUserDashboardStats(Long userId);

    List<ActivityResponse> getRecentActivities(int limit);

    /** Keys: total, draft, underReview, approved, rejected, archived. */
    Map<String, Long> getDocumentStats();

    /** Keys: total, pending, inProgress, approved, rejected, expired. */
    Map<String, Long> getWorkflowStats();

    /** Keys: total, active, inactive. */
    Map<String, Long> getUserStats();
}