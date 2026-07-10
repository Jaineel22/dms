package com.dms.repository;

import com.dms.entity.WorkflowInstance;
import com.dms.entity.WorkflowInstance.WorkflowInstanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {

    Optional<WorkflowInstance> findByDocumentId(Long documentId);

    List<WorkflowInstance> findByCurrentApproverIdAndStatus(Long currentApproverId, WorkflowInstanceStatus status);

    Page<WorkflowInstance> findByCurrentApproverId(Long currentApproverId, Pageable pageable);

    Page<WorkflowInstance> findBySubmittedById(Long submittedById, Pageable pageable);

    Page<WorkflowInstance> findByStatus(WorkflowInstanceStatus status, Pageable pageable);

    @Query("SELECT wi FROM WorkflowInstance wi " +
           "WHERE wi.isActive = true AND wi.status = 'IN_PROGRESS' " +
           "AND wi.deadlineAt < :now")
    List<WorkflowInstance> findExpiredActiveInstances(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE WorkflowInstance wi SET wi.isActive = false WHERE wi.id = :id")
    void deactivateInstance(@Param("id") Long id);
}