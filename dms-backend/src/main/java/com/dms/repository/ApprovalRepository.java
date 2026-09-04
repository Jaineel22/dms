package com.dms.repository;

import com.dms.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByWorkflowInstanceIdOrderByPerformedAtDesc(Long workflowInstanceId);

    Optional<Approval> findByWorkflowInstanceIdAndIsCurrentTrue(Long workflowInstanceId);

    /** True if {@code approverId} is the assigned approver of the current approval for this workflow instance. */
    boolean existsByWorkflowInstanceIdAndApproverIdAndIsCurrentTrue(Long workflowInstanceId, Long approverId);

    /** True if {@code approverId} has a current (pending) approval for any workflow instance of this document. */
    boolean existsByApproverIdAndWorkflowInstanceDocumentIdAndIsCurrentTrue(Long approverId, Long documentId);

    @Query("SELECT a FROM Approval a WHERE a.workflowInstance.id = :workflowInstanceId AND a.isCurrent = true")
    Optional<Approval> findCurrentApproval(@Param("workflowInstanceId") Long workflowInstanceId);

    @Modifying
    @Query("UPDATE Approval a SET a.isCurrent = false WHERE a.workflowInstance.id = :workflowInstanceId")
    void markPreviousApprovalsAsNotCurrent(@Param("workflowInstanceId") Long workflowInstanceId);
}