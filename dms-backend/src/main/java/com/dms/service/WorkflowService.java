package com.dms.service;

import com.dms.dto.request.WorkflowCreateRequest;
import com.dms.dto.request.WorkflowUpdateRequest;
import com.dms.dto.response.WorkflowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkflowService {

    /**
     * Creates a new workflow with its steps.
     * Validates name uniqueness, step sequencing, approval levels, and role names.
     */
    WorkflowResponse createWorkflow(WorkflowCreateRequest request);

    /**
     * Updates metadata (name, description, department, isActive) of an existing workflow.
     * Steps are not updated via this method — use createWorkflow for full replacement.
     */
    WorkflowResponse updateWorkflow(Long workflowId, WorkflowUpdateRequest request);

    /**
     * Soft-deletes a workflow (sets isActive = false).
     */
    void deleteWorkflow(Long workflowId);

    /**
     * Returns a workflow by ID without eagerly loading steps.
     */
    WorkflowResponse getWorkflowById(Long workflowId);

    /**
     * Returns a paginated list of all active workflows.
     */
    Page<WorkflowResponse> getAllWorkflows(Pageable pageable);

    /**
     * Returns all active workflows for a specific department,
     * plus all globally applicable workflows (no department restriction).
     */
    List<WorkflowResponse> getWorkflowsByDepartment(Long departmentId);

    /**
     * Assigns a workflow to a user.
     * Throws if the assignment already exists.
     */
    void assignWorkflowToUser(Long userId, Long workflowId);

    /**
     * Deactivates a user's workflow assignment (soft-remove).
     */
    void removeWorkflowFromUser(Long userId, Long workflowId);

    /**
     * Returns all active workflow assignments for a user, with steps included.
     */
    List<WorkflowResponse> getUserWorkflows(Long userId);

    /**
     * Returns a workflow with its steps eagerly fetched (avoids N+1).
     */
    WorkflowResponse getWorkflowWithSteps(Long workflowId);
}