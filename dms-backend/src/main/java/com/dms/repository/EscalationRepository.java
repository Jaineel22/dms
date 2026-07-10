package com.dms.repository;

import com.dms.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, Long> {

    List<Escalation> findByWorkflowInstanceIdOrderByEscalatedAtDesc(Long workflowInstanceId);

    List<Escalation> findByResolvedAtIsNull();

    List<Escalation> findByToUserIdAndResolvedAtIsNull(Long toUserId);
}