package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.AuditLogResponse;
import com.dms.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
@Tag(name = "Audit Logs", description = "System-wide audit trail — admin only")
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Get all audit logs, most recent first")
    @GetMapping(ApiConstants.AUDIT_BASE)
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllAuditLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> response = auditService.getRecentActivity(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get audit logs for a specific user")
    @GetMapping(ApiConstants.AUDIT_USER)
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getUserAuditTrail(
            @PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> response = auditService.getUserAuditTrail(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get audit logs for a specific entity")
    @GetMapping(ApiConstants.AUDIT_ENTITY)
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getEntityAuditTrail(
            @PathVariable String entityType, @PathVariable Long entityId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> response = auditService.getAuditTrail(entityType, entityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get audit logs filtered by action")
    @GetMapping(ApiConstants.AUDIT_ACTION)
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAuditTrailByAction(
            @PathVariable String action, @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> response = auditService.getAuditTrailByAction(action, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get the most recent activity across the system (default limit 20)")
    @GetMapping(ApiConstants.AUDIT_RECENT)
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getRecentActivity(
            @RequestParam(defaultValue = "20") int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLogResponse> response = auditService.getRecentActivity(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}