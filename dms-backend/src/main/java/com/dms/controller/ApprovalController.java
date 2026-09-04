package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.ApproveRequest;
import com.dms.dto.request.RejectRequest;
import com.dms.dto.request.SendBackRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.ApprovalResponse;
import com.dms.service.ApprovalService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Approvals", description = "Approve, reject, send back, and escalate workflow steps")
public class ApprovalController {

    private final ApprovalService approvalService;

    @Operation(summary = "Approve the current step of a workflow instance")
    @PostMapping(ApiConstants.APPROVAL_APPROVE)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<ApprovalResponse>> approve(
            @PathVariable Long id, @Valid @ModelAttribute ApproveRequest request) {
        request.setApprovalId(id);
        log.info("Approving approval {}", id);
        ApprovalResponse response = approvalService.approve(request);
        return ResponseEntity.ok(ApiResponse.success("Approved successfully", response));
    }

    @Operation(summary = "Reject the current step of a workflow instance")
    @PostMapping(ApiConstants.APPROVAL_REJECT)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<ApprovalResponse>> reject(
            @PathVariable Long id, @Valid @ModelAttribute RejectRequest request) {
        request.setApprovalId(id);
        log.info("Rejecting approval {}", id);
        ApprovalResponse response = approvalService.reject(request);
        return ResponseEntity.ok(ApiResponse.success("Rejected successfully", response));
    }

    @Operation(summary = "Send the document back for changes")
    @PostMapping(ApiConstants.APPROVAL_SEND_BACK)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<ApprovalResponse>> sendBack(
            @PathVariable Long id, @Valid @ModelAttribute SendBackRequest request) {
        request.setApprovalId(id);
        log.info("Sending back approval {}", id);
        ApprovalResponse response = approvalService.sendBack(request);
        return ResponseEntity.ok(ApiResponse.success("Sent back successfully", response));
    }

    @Operation(summary = "Get the current active approval for a workflow instance")
    @GetMapping(ApiConstants.APPROVAL_CURRENT)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<ApprovalResponse>> getCurrentApproval(@PathVariable Long instanceId) {
        ApprovalResponse response = approvalService.getCurrentApproval(instanceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get the full approval history for a workflow instance")
    @GetMapping(ApiConstants.APPROVAL_HISTORY)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<Page<ApprovalResponse>>> getApprovalHistory(
            @PathVariable Long instanceId, @PageableDefault(size = 20) Pageable pageable) {
        Page<ApprovalResponse> response = approvalService.getApprovalHistory(instanceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Escalate a pending workflow instance to another approver")
    @PostMapping(ApiConstants.APPROVAL_ESCALATE)
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isCurrentApprover(#id)")
    public ResponseEntity<ApiResponse<Void>> escalate(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long toUserId) {
        log.info("Escalating workflow instance {} to user {}", id, toUserId);
        approvalService.escalateApproval(id, reason, toUserId);
        return ResponseEntity.ok(ApiResponse.success("Escalated successfully"));
    }
}