package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.SubmitDocumentRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.ApprovalResponse;
import com.dms.dto.response.PendingApprovalResponse;
import com.dms.dto.response.WorkflowInstanceResponse;
import com.dms.dto.response.WorkflowSummaryResponse;
import com.dms.security.SecurityUtils;
import com.dms.service.WorkflowExecutionService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Workflow Execution", description = "Submit documents for approval and track workflow instances")
public class WorkflowExecutionController {

    private final WorkflowExecutionService workflowExecutionService;

    @Operation(summary = "Submit a document for approval")
    @PostMapping(ApiConstants.WORKFLOW_SUBMIT)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> submitDocument(
            @Valid @RequestBody SubmitDocumentRequest request) {
        log.info("Submitting document {} for approval", request.getDocumentId());
        WorkflowInstanceResponse response = workflowExecutionService.submitDocument(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Document submitted for approval successfully"));
    }

    @Operation(summary = "Get a workflow instance by id")
    @GetMapping(ApiConstants.WORKFLOW_INSTANCE_BY_ID)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<WorkflowInstanceResponse>> getWorkflowInstance(@PathVariable Long id) {
        WorkflowInstanceResponse response = workflowExecutionService.getWorkflowInstance(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get pending approvals assigned to the current user")
    @GetMapping(ApiConstants.WORKFLOW_PENDING)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<Page<PendingApprovalResponse>>> getPendingApprovals(
            @PageableDefault(size = 20) Pageable pageable) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Page<PendingApprovalResponse> response = workflowExecutionService.getPendingApprovals(currentUserId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get approval history for a document")
    @GetMapping(ApiConstants.WORKFLOW_HISTORY)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<List<ApprovalResponse>>> getApprovalHistory(@PathVariable Long documentId) {
        List<ApprovalResponse> response = workflowExecutionService.getApprovalHistory(documentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get all workflow instances submitted by a user")
    @GetMapping(ApiConstants.WORKFLOW_USER)
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<Page<WorkflowInstanceResponse>>> getInstancesByUser(
            @PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable) {
        Page<WorkflowInstanceResponse> response = workflowExecutionService.getInstancesByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get workflow summary statistics")
    @GetMapping(ApiConstants.WORKFLOW_SUMMARY)
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<WorkflowSummaryResponse>> getWorkflowSummary() {
        WorkflowSummaryResponse response = workflowExecutionService.getWorkflowSummary();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Cancel an active workflow instance")
    @DeleteMapping(ApiConstants.WORKFLOW_CANCEL)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<Void>> cancelWorkflow(@PathVariable Long id) {
        workflowExecutionService.cancelWorkflow(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Workflow cancelled successfully"));
    }
}