package com.dms.validator;

import com.dms.constant.ErrorConstants;
import com.dms.dto.request.WorkflowStepRequest;
import com.dms.entity.Approval;
import com.dms.entity.Document;
import com.dms.entity.WorkflowInstance;
import com.dms.exception.DocumentException;
import com.dms.exception.WorkflowException;
import com.dms.exception.WorkflowExecutionException;
import com.dms.repository.ApprovalRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.RoleRepository;
import com.dms.repository.UserWorkflowRepository;
import com.dms.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring-managed validator for workflow operations.
 * Centralises business rules for both workflow definition and execution.
 */
@Component
@RequiredArgsConstructor
public class WorkflowValidator {

    // ─── Phase 2 Dependencies (Workflow Definition) ────────────────────────────
    private final RoleRepository roleRepository;

    // ─── Phase 4 Dependencies (Workflow Execution) ─────────────────────────────
    private final DocumentRepository documentRepository;
    private final UserWorkflowRepository userWorkflowRepository;
    private final ApprovalRepository approvalRepository;
    private final WorkflowStepRepository workflowStepRepository;

    // ═════════════════════════════════════════════════════════════════════════════
    //  PHASE 2 — Workflow Definition Validation
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * Validates workflow steps:
     * <ul>
     *   <li>At least 2 steps</li>
     *   <li>Steps start at 1</li>
     *   <li>Steps are sequential without gaps</li>
     *   <li>No duplicate step numbers</li>
     *   <li>Approval levels are between 1-4</li>
     *   <li>Role names exist in the system</li>
     * </ul>
     *
     * @throws WorkflowException if any rule is violated
     */
    public void validateWorkflowSteps(List<WorkflowStepRequest> steps) {
        if (steps == null || steps.size() < 2) {
            throw new WorkflowException("Workflow must have at least 2 steps");
        }

        // Check step numbers start at 1
        int minStep = steps.stream()
                .mapToInt(WorkflowStepRequest::getStepNumber)
                .min()
                .orElse(0);

        if (minStep != 1) {
            throw new WorkflowException("Step numbers must start at 1");
        }

        // Check for duplicates
        Set<Integer> stepNumbers = steps.stream()
                .map(WorkflowStepRequest::getStepNumber)
                .collect(Collectors.toSet());

        if (stepNumbers.size() != steps.size()) {
            throw new WorkflowException("Duplicate step numbers are not allowed");
        }

        // Check sequential order (1, 2, 3, ...)
        List<WorkflowStepRequest> sorted = steps.stream()
                .sorted((a, b) -> Integer.compare(a.getStepNumber(), b.getStepNumber()))
                .collect(Collectors.toList());

        int expectedStep = 1;
        for (WorkflowStepRequest step : sorted) {
            if (!step.getStepNumber().equals(expectedStep)) {
                throw new WorkflowException(
                        String.format("Steps must be sequential. Expected step %d but found %d",
                                expectedStep, step.getStepNumber()));
            }
            expectedStep++;
        }

        // Validate approval levels (1-4)
        for (WorkflowStepRequest step : steps) {
            if (step.getApprovalLevel() < 1 || step.getApprovalLevel() > 4) {
                throw new WorkflowException(
                        String.format("Approval level %d is invalid. Must be between 1 and 4",
                                step.getApprovalLevel()));
            }
        }

        // Validate role names exist
        for (WorkflowStepRequest step : steps) {
            String roleName = step.getRoleName();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            boolean roleExists = roleRepository.existsByName(roleName);
            if (!roleExists) {
                throw new WorkflowException(
                        String.format("Role '%s' does not exist in the system", step.getRoleName()));
            }
        }
    }

    /**
     * Alias kept for blueprint/API compatibility — delegates to
     * {@link #validateWorkflowSteps(List)} which already covers ordering.
     */
    public void validateStepOrder(List<WorkflowStepRequest> steps) {
        validateWorkflowSteps(steps);
    }

    /**
     * Alias kept for blueprint/API compatibility — delegates to
     * {@link #validateWorkflowSteps(List)} which already covers approval levels.
     */
    public void validateApprovalLevels(List<WorkflowStepRequest> steps) {
        validateWorkflowSteps(steps);
    }

    // ═════════════════════════════════════════════════════════════════════════════
    //  PHASE 4 — Workflow Execution Validation
    // ═════════════════════════════════════════════════════════════════════════════

    /**
     * Ensures a document exists, is not archived, and has no active workflow instance.
     */
    public void validateDocumentCanBeSubmitted(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException(ErrorConstants.WORKFLOW_NOT_FOUND));

        if (Boolean.TRUE.equals(document.getIsArchived())) {
            throw new WorkflowExecutionException("Cannot submit an archived document for approval");
        }

        if (document.getWorkflowInstanceId() != null && "UNDER_REVIEW".equals(document.getWorkflowStatus())) {
            throw new WorkflowExecutionException(ErrorConstants.WORKFLOW_ALREADY_SUBMITTED);
        }
    }

    /**
     * Ensures the given user has a workflow assigned (used when no explicit workflowId is provided).
     */
    public void validateUserHasWorkflow(Long userId) {
        userWorkflowRepository.findByUserId(userId)
                .orElseThrow(() -> new WorkflowExecutionException(ErrorConstants.WORKFLOW_NO_WORKFLOW_ASSIGNED));
    }

    /**
     * Ensures the given user is the approver assigned to the given approval.
     */
    public void validateApprover(Long approvalId, Long userId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new WorkflowExecutionException(ErrorConstants.APPROVAL_NOT_FOUND));

        if (approval.getApprover() == null || !approval.getApprover().getId().equals(userId)) {
            throw new WorkflowExecutionException(ErrorConstants.APPROVAL_UNAUTHORIZED);
        }
    }

    /**
     * Ensures the given approval is still the active/current one for its workflow instance.
     */
    public void validateApprovalIsCurrent(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new WorkflowExecutionException(ErrorConstants.APPROVAL_NOT_FOUND));

        if (!Boolean.TRUE.equals(approval.getIsCurrent())) {
            throw new WorkflowExecutionException(ErrorConstants.APPROVAL_NOT_CURRENT);
        }
    }

    /**
     * Ensures the target step number exists within the workflow instance's definition.
     */
    public void validateWorkflowStepTransition(WorkflowInstance instance, Integer stepNumber) {
        workflowStepRepository
                .findByWorkflowDefinitionIdAndStepNumber(instance.getWorkflowDefinition().getId(), stepNumber)
                .orElseThrow(() -> new WorkflowExecutionException(ErrorConstants.WORKFLOW_INVALID_STEP));
    }
}