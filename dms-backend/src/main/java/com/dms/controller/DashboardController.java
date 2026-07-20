package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.response.ActivityResponse;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.DashboardStatsResponse;
import com.dms.security.SecurityUtils;
import com.dms.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated statistics and recent activity")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get full dashboard statistics")
    @GetMapping(ApiConstants.DASHBOARD_STATS)
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN_OR_VIEWER)
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse response = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get dashboard statistics scoped to the current user")
    @GetMapping(ApiConstants.DASHBOARD_USER_STATS)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getUserDashboardStats() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        DashboardStatsResponse response = dashboardService.getUserDashboardStats(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get recent activity across the system (default limit 10)")
    @GetMapping(ApiConstants.DASHBOARD_RECENT_ACTIVITY)
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN_OR_VIEWER)
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit) {
        List<ActivityResponse> response = dashboardService.getRecentActivities(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get document statistics breakdown")
    @GetMapping(ApiConstants.DASHBOARD_DOCUMENT_STATS)
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN_OR_VIEWER)
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDocumentStats() {
        Map<String, Long> response = dashboardService.getDocumentStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get workflow statistics breakdown")
    @GetMapping(ApiConstants.DASHBOARD_WORKFLOW_STATS)
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN_OR_VIEWER)
    public ResponseEntity<ApiResponse<Map<String, Long>>> getWorkflowStats() {
        Map<String, Long> response = dashboardService.getWorkflowStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}