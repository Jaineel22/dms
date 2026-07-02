package com.dms.repository;

import com.dms.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {

    // ─── List queries ─────────────────────────────────────────────────────────

    /** Returns all steps for a workflow ordered by step_number ASC. */
    List<WorkflowStep> findByWorkflowIdOrderByStepNumberAsc(Long workflowId);

    // ─── Existence checks ────────────────────────────────────────────────────

    Boolean existsByWorkflowIdAndStepNumber(Long workflowId, Integer stepNumber);

    // ─── Delete helpers ───────────────────────────────────────────────────────

    /**
     * Deletes all steps belonging to a workflow.
     * Prefer this over the CascadeType.ALL path when you want explicit control,
     * e.g. before replacing an entire step set atomically.
     */
    @Modifying
    @Query("DELETE FROM WorkflowStep s WHERE s.workflow.id = :workflowId")
    void deleteByWorkflowId(@Param("workflowId") Long workflowId);

    /** Alias — same as deleteByWorkflowId; kept for blueprint compatibility. */
    @Modifying
    @Query("DELETE FROM WorkflowStep s WHERE s.workflow.id = :workflowId")
    void deleteAllByWorkflowId(@Param("workflowId") Long workflowId);

    // ─── Custom queries ───────────────────────────────────────────────────────

    /**
     * Find the step at a specific approval level within a workflow.
     * Useful when routing a document approval to the correct approver.
     */
    Optional<WorkflowStep> findByWorkflowIdAndApprovalLevel(Long workflowId, Integer approvalLevel);

    /**
     * Count steps in a workflow — used to determine if we're at the final step.
     */
    long countByWorkflowId(Long workflowId);

    /**
     * Find the maximum step number for a given workflow.
     * Returns null if the workflow has no steps yet.
     */
    @Query("SELECT MAX(s.stepNumber) FROM WorkflowStep s WHERE s.workflow.id = :workflowId")
    Integer findMaxStepNumberByWorkflowId(@Param("workflowId") Long workflowId);
}