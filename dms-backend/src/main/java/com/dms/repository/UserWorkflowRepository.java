package com.dms.repository;

import com.dms.entity.UserWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWorkflowRepository extends JpaRepository<UserWorkflow, Long> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    /** Returns the active-only workflow assignments for a user. */
    List<UserWorkflow> findByUserIdAndIsActiveTrue(Long userId);

    /** Returns all workflow assignments for a user (active + inactive). */
    List<UserWorkflow> findByUserId(Long userId);

    /** Returns the most recent active assignments for a user, newest first. */
    List<UserWorkflow> findByUserIdAndIsActiveTrueOrderByAssignedAtDesc(Long userId);

    /** Returns all users assigned to a specific workflow. */
    List<UserWorkflow> findByWorkflowIdAndIsActiveTrue(Long workflowId);

    /**
     * Finds the unique mapping between a user and a workflow (active or inactive).
     * Used to check for duplicates before assigning.
     */
    Optional<UserWorkflow> findByUserIdAndWorkflowId(Long userId, Long workflowId);

    // ─── Existence checks ────────────────────────────────────────────────────

    Boolean existsByUserIdAndWorkflowId(Long userId, Long workflowId);

    // ─── Delete ───────────────────────────────────────────────────────────────

    /**
     * Hard-deletes the mapping between a user and a specific workflow.
     * Prefer soft-delete (setting isActive = false) in business logic.
     */
    @Modifying
    @Query("DELETE FROM UserWorkflow uw WHERE uw.userId = :userId AND uw.workflow.id = :workflowId")
    void deleteByUserIdAndWorkflowId(
            @Param("userId")     Long userId,
            @Param("workflowId") Long workflowId);

    // ─── Custom queries ───────────────────────────────────────────────────────

    /**
     * Soft-deactivate all workflow assignments for a user.
     * Called when a user is deactivated so their workflows also pause.
     */
    @Modifying
    @Query("UPDATE UserWorkflow uw SET uw.isActive = false WHERE uw.userId = :userId")
    void deactivateAllForUser(@Param("userId") Long userId);

    /**
     * Count how many active users are assigned to a workflow.
     * Useful for the workflow management UI.
     */
    @Query("SELECT COUNT(uw) FROM UserWorkflow uw WHERE uw.workflow.id = :workflowId AND uw.isActive = true")
    long countActiveAssignmentsByWorkflowId(@Param("workflowId") Long workflowId);

    /**
     * Check whether a specific workflow assignment is currently active.
     */
    Boolean existsByUserIdAndWorkflowIdAndIsActiveTrue(Long userId, Long workflowId);
}