package com.dms.validator;

import com.dms.dto.request.WorkflowStepRequest;
import com.dms.exception.WorkflowException;
import com.dms.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring-managed validator for workflow step lists.
 * Centralises the business rules so they can be reused outside
 * {@code WorkflowServiceImpl} (e.g. from a future bulk-import feature).
 */
@Component
@RequiredArgsConstructor
public class WorkflowValidator {

    private final RoleRepository roleRepository;

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

        // Check sequential order (1, 2, 3, ...) — assumes input order represents intended order
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
}