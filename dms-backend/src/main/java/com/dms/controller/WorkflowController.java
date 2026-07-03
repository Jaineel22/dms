package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.WorkflowCreateRequest;
import com.dms.dto.request.WorkflowUpdateRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.WorkflowResponse;
import com.dms.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.WORKFLOW_BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Workflow Management", description = "Manage approval workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    // ─── POST /workflows ──────────────────────────────────────────────────────

    @Operation(summary = "Create workflow (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Workflow created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Workflow name already exists")
    })
    @PostMapping
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<WorkflowResponse>> createWorkflow(
            @Valid @RequestBody WorkflowCreateRequest request) {

        WorkflowResponse response = workflowService.createWorkflow(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workflow created successfully", response));
    }

    // ─── PUT /workflows/{workflowId} ──────────────────────────────────────────

    @Operation(summary = "Update workflow (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Workflow name already exists")
    })
    @PutMapping("/{workflowId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<WorkflowResponse>> updateWorkflow(
            @Parameter(description = "ID of the workflow") @PathVariable Long workflowId,
            @Valid @RequestBody WorkflowUpdateRequest request) {

        WorkflowResponse response = workflowService.updateWorkflow(workflowId, request);
        return ResponseEntity.ok(ApiResponse.success("Workflow updated successfully", response));
    }

    // ─── DELETE /workflows/{workflowId} ───────────────────────────────────────

    @Operation(summary = "Delete workflow (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Workflow deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    @DeleteMapping("/{workflowId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> deleteWorkflow(
            @Parameter(description = "ID of the workflow") @PathVariable Long workflowId) {

        workflowService.deleteWorkflow(workflowId);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /workflows/{workflowId} ──────────────────────────────────────────

    @Operation(summary = "Get workflow by ID (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    @GetMapping("/{workflowId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflowById(
            @Parameter(description = "ID of the workflow") @PathVariable Long workflowId) {

        WorkflowResponse response = workflowService.getWorkflowById(workflowId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /workflows/{workflowId}/steps ────────────────────────────────────

    @Operation(summary = "Get workflow with steps (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow with steps returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    @GetMapping("/{workflowId}/steps")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflowWithSteps(
            @Parameter(description = "ID of the workflow") @PathVariable Long workflowId) {

        WorkflowResponse response = workflowService.getWorkflowWithSteps(workflowId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /workflows ────────────────────────────────────────────────────────

    @Operation(summary = "Get all workflows (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflows returned")
    })
    @GetMapping
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<Page<WorkflowResponse>>> getAllWorkflows(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field,direction") @RequestParam(defaultValue = "name,asc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        Page<WorkflowResponse> response = workflowService.getAllWorkflows(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /workflows/department/{departmentId} ─────────────────────────────

    @Operation(summary = "Get workflows by department (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department workflows returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/department/{departmentId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<List<WorkflowResponse>>> getWorkflowsByDepartment(
            @Parameter(description = "ID of the department") @PathVariable Long departmentId) {

        List<WorkflowResponse> response = workflowService.getWorkflowsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── POST /workflows/{workflowId}/assign/{userId} ─────────────────────────

    @Operation(summary = "Assign workflow to user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or Workflow not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Workflow already assigned to user")
    })
    @PostMapping("/{workflowId}/assign/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<Void>> assignWorkflowToUser(
            @Parameter(description = "ID of the workflow") @PathVariable Long workflowId,
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        workflowService.assignWorkflowToUser(userId, workflowId);
        return ResponseEntity.ok(ApiResponse.success("Workflow assigned to user successfully"));
    }

    // ─── DELETE /workflows/{workflowId}/assign/{userId} ───────────────────────

    @Operation(summary = "Remove workflow from user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Workflow removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or Workflow not found")
    })
    @DeleteMapping("/{workflowId}/assign/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> removeWorkflowFromUser(
            @Parameter(description = "ID of the workflow") @PathVariable Long workflowId,
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        workflowService.removeWorkflowFromUser(userId, workflowId);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /workflows/user/{userId} ──────────────────────────────────────────

    @Operation(summary = "Get workflows assigned to user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User workflows returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<List<WorkflowResponse>>> getUserWorkflows(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        List<WorkflowResponse> response = workflowService.getUserWorkflows(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sort) {
        int safeSize = Math.min(size, 100);
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, safeSize, Sort.by(dir, field));
    }
}