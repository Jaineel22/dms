package com.dms.service.impl;

import com.dms.dto.request.SubmitDocumentRequest;
import com.dms.dto.response.ApprovalResponse;
import com.dms.dto.response.PendingApprovalResponse;
import com.dms.dto.response.WorkflowInstanceResponse;
import com.dms.dto.response.WorkflowSummaryResponse;
import com.dms.entity.Approval;
import com.dms.entity.Document;
import com.dms.entity.User;
import com.dms.entity.UserWorkflow;
import com.dms.entity.WorkflowDefinition;
import com.dms.entity.WorkflowInstance;
import com.dms.entity.WorkflowStep;
import com.dms.exception.DocumentException;
import com.dms.exception.WorkflowException;
import com.dms.mapper.ApprovalMapper;
import com.dms.mapper.WorkflowInstanceMapper;
import com.dms.repository.ApprovalRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.UserRepository;
import com.dms.repository.UserWorkflowRepository;
import com.dms.repository.WorkflowDefinitionRepository;
import com.dms.repository.WorkflowInstanceRepository;
import com.dms.repository.WorkflowStepRepository;
import com.dms.security.SecurityUtils;
import com.dms.service.HierarchyService;
import com.dms.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * NOTE on architecture: the step-transition logic for approve/reject/send-back
 * (moving to the next step, closing out the workflow, updating the document status)
 * lives in {@link com.dms.service.impl.ApprovalServiceImpl}, since those operations
 * are exposed on the {@code ApprovalService} contract. This service owns submission,
 * querying, and lifecycle/reporting concerns for workflow instances.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {

    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final ApprovalRepository approvalRepository;
    private final DocumentRepository documentRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final UserWorkflowRepository userWorkflowRepository;
    private final UserRepository userRepository;
    private final HierarchyService hierarchyService;
    private final WorkflowInstanceMapper workflowInstanceMapper;
    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional
    public WorkflowInstanceResponse submitDocument(SubmitDocumentRequest request) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new DocumentException("Document not found with id: " + request.getDocumentId()));

        if (document.getWorkflowInstanceId() != null && "UNDER_REVIEW".equals(document.getWorkflowStatus())) {
            throw new WorkflowException("Document is already under an active workflow");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User submitter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new WorkflowException("Current user not found"));

        WorkflowDefinition workflowDefinition;
        if (request.getWorkflowId() != null) {
            workflowDefinition = workflowDefinitionRepository.findById(request.getWorkflowId())
                    .orElseThrow(() -> new WorkflowException(
                            "Workflow definition not found with id: " + request.getWorkflowId()));
        } else {
            workflowDefinition = userWorkflowRepository.findByUserId(currentUserId)
                    .map(UserWorkflow::getWorkflowDefinition)
                    .orElseThrow(() -> new WorkflowException("No default workflow assigned to user"));
        }

        WorkflowStep firstStep = workflowStepRepository
                .findFirstByWorkflowDefinitionIdOrderByStepNumberAsc(workflowDefinition.getId())
                .orElseThrow(() -> new WorkflowException("Workflow has no steps configured"));

        User approver = hierarchyService.getApproverForLevel(currentUserId, firstStep.getApprovalLevel());
        if (approver == null) {
            throw new WorkflowException("No approver found for step level: " + firstStep.getApprovalLevel());
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.setDocument(document);
        instance.setWorkflowDefinition(workflowDefinition);
        instance.setCurrentStep(firstStep);
        instance.setCurrentApprover(approver);
        instance.setStatus(WorkflowInstance.WorkflowInstanceStatus.PENDING);
        instance.setSubmittedBy(submitter);
        instance.setSubmittedAt(LocalDateTime.now());
        instance.setIsActive(true);
        if (firstStep.getTimeoutHours() != null) {
            instance.setDeadlineAt(LocalDateTime.now().plusHours(firstStep.getTimeoutHours()));
        }

        WorkflowInstance saved = workflowInstanceRepository.save(instance);

        Approval firstApproval = new Approval();
        firstApproval.setWorkflowInstance(saved);
        firstApproval.setStep(firstStep);
        firstApproval.setApprover(approver);
        firstApproval.setIsCurrent(true);
        firstApproval.setComments(request.getComments());
        approvalRepository.save(firstApproval);

        document.setWorkflowStatus("UNDER_REVIEW");
        document.setWorkflowInstanceId(saved.getId());
        documentRepository.save(document);

        log.info("Document {} submitted to workflow {} by user {}", document.getId(),
                workflowDefinition.getId(), currentUserId);

        return workflowInstanceMapper.toResponse(saved);
    }

    @Override
    public WorkflowInstanceResponse getWorkflowInstance(Long instanceId) {
        return workflowInstanceMapper.toResponse(getInstanceOrThrow(instanceId));
    }

    @Override
    public Page<PendingApprovalResponse> getPendingApprovals(Long userId, Pageable pageable) {
        return workflowInstanceRepository.findByCurrentApproverId(userId, pageable)
                .map(instance -> workflowInstanceMapper.toPendingApprovalResponse(instance, instance.getDocument()));
    }

    @Override
    public List<ApprovalResponse> getApprovalHistory(Long documentId) {
        WorkflowInstance instance = workflowInstanceRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new WorkflowException("No workflow instance found for document: " + documentId));

        List<Approval> approvals = approvalRepository.findByWorkflowInstanceIdOrderByPerformedAtDesc(instance.getId());
        return approvalMapper.toResponseList(approvals);
    }

    @Override
    public Page<WorkflowInstanceResponse> getInstancesByUser(Long userId, Pageable pageable) {
        return workflowInstanceRepository.findBySubmittedById(userId, pageable)
                .map(workflowInstanceMapper::toResponse);
    }

    @Override
    public WorkflowSummaryResponse getWorkflowSummary() {
        List<WorkflowInstance> all = workflowInstanceRepository.findAll();

        double avgHours = all.stream()
                .filter(wi -> wi.getSubmittedAt() != null && wi.getCompletedAt() != null)
                .mapToLong(wi -> Duration.between(wi.getSubmittedAt(), wi.getCompletedAt()).toHours())
                .average()
                .orElse(0.0);

        return WorkflowSummaryResponse.builder()
                .totalInstances((long) all.size())
                .pendingInstances(countByStatus(all, WorkflowInstance.WorkflowInstanceStatus.PENDING))
                .inProgressInstances(countByStatus(all, WorkflowInstance.WorkflowInstanceStatus.IN_PROGRESS))
                .approvedInstances(countByStatus(all, WorkflowInstance.WorkflowInstanceStatus.APPROVED))
                .rejectedInstances(countByStatus(all, WorkflowInstance.WorkflowInstanceStatus.REJECTED))
                .expiredInstances(countByStatus(all, WorkflowInstance.WorkflowInstanceStatus.EXPIRED))
                .averageCompletionTimeHours(avgHours)
                .build();
    }

    @Override
    @Transactional
    public void cancelWorkflow(Long instanceId) {
        WorkflowInstance instance = getInstanceOrThrow(instanceId);

        if (!Boolean.TRUE.equals(instance.getIsActive())) {
            throw new WorkflowException("Workflow instance is already inactive");
        }

        workflowInstanceRepository.deactivateInstance(instanceId);

        Document document = instance.getDocument();
        document.setWorkflowStatus("CANCELLED");
        document.setWorkflowInstanceId(null);
        documentRepository.save(document);

        log.info("Workflow instance {} cancelled", instanceId);
    }

    private WorkflowInstance getInstanceOrThrow(Long instanceId) {
        return workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new WorkflowException("Workflow instance not found with id: " + instanceId));
    }

    private long countByStatus(List<WorkflowInstance> instances, WorkflowInstance.WorkflowInstanceStatus status) {
        return instances.stream().filter(i -> i.getStatus() == status).count();
    }
}