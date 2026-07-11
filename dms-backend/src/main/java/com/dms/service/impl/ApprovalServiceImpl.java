package com.dms.service.impl;

import com.dms.dto.request.ApproveRequest;
import com.dms.dto.request.RejectRequest;
import com.dms.dto.request.SendBackRequest;
import com.dms.dto.response.ApprovalResponse;
import com.dms.entity.Approval;
import com.dms.entity.Document;
import com.dms.entity.Escalation;
import com.dms.entity.User;
import com.dms.entity.WorkflowInstance;
import com.dms.entity.WorkflowStep;
import com.dms.exception.WorkflowException;
import com.dms.mapper.ApprovalMapper;
import com.dms.repository.ApprovalRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.EscalationRepository;
import com.dms.repository.UserRepository;
import com.dms.repository.WorkflowInstanceRepository;
import com.dms.repository.WorkflowStepRepository;
import com.dms.security.SecurityUtils;
import com.dms.service.ApprovalService;
import com.dms.service.HierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final DocumentRepository documentRepository;
    private final EscalationRepository escalationRepository;
    private final UserRepository userRepository;
    private final HierarchyService hierarchyService;
    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional
    public ApprovalResponse approve(ApproveRequest request) {
        Approval currentApproval = getCurrentApprovalOrThrow(request.getApprovalId());
        WorkflowInstance instance = currentApproval.getWorkflowInstance();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        validateApprover(currentApproval, currentUserId);

        closeOutApproval(currentApproval, Approval.ApprovalAction.APPROVED, request.getComments(), request.getAttachment());

        WorkflowStep nextStep = workflowStepRepository
                .findByWorkflowDefinitionIdAndStepNumber(
                        instance.getWorkflowDefinition().getId(),
                        currentApproval.getStep().getStepNumber() + 1)
                .orElse(null);

        Document document = instance.getDocument();

        if (nextStep == null) {
            instance.setStatus(WorkflowInstance.WorkflowInstanceStatus.APPROVED);
            instance.setCompletedAt(LocalDateTime.now());
            instance.setCurrentStep(null);
            instance.setCurrentApprover(null);

            document.setWorkflowStatus("APPROVED");
            document.setApprovedAt(LocalDateTime.now());
            document.setApprovedBy(currentApproval.getApprover());
        } else {
            User nextApprover = hierarchyService.getApproverForLevel(
                    instance.getSubmittedBy().getId(), nextStep.getApprovalLevel());
            if (nextApprover == null) {
                throw new WorkflowException("No approver found for step level: " + nextStep.getApprovalLevel());
            }

            instance.setStatus(WorkflowInstance.WorkflowInstanceStatus.IN_PROGRESS);
            instance.setCurrentStep(nextStep);
            instance.setCurrentApprover(nextApprover);
            if (nextStep.getTimeoutHours() != null) {
                instance.setDeadlineAt(LocalDateTime.now().plusHours(nextStep.getTimeoutHours()));
            }

            Approval nextApproval = new Approval();
            nextApproval.setWorkflowInstance(instance);
            nextApproval.setStep(nextStep);
            nextApproval.setApprover(nextApprover);
            nextApproval.setIsCurrent(true);
            approvalRepository.save(nextApproval);
        }

        workflowInstanceRepository.save(instance);
        documentRepository.save(document);

        log.info("Approval {} approved by user {}", currentApproval.getId(), currentUserId);
        return approvalMapper.toResponse(currentApproval);
    }

    @Override
    @Transactional
    public ApprovalResponse reject(RejectRequest request) {
        Approval currentApproval = getCurrentApprovalOrThrow(request.getApprovalId());
        WorkflowInstance instance = currentApproval.getWorkflowInstance();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        validateApprover(currentApproval, currentUserId);

        closeOutApproval(currentApproval, Approval.ApprovalAction.REJECTED, request.getComments(), request.getAttachment());

        instance.setStatus(WorkflowInstance.WorkflowInstanceStatus.REJECTED);
        instance.setCompletedAt(LocalDateTime.now());
        instance.setCurrentStep(null);
        instance.setCurrentApprover(null);
        workflowInstanceRepository.save(instance);

        Document document = instance.getDocument();
        document.setWorkflowStatus("REJECTED");
        documentRepository.save(document);

        log.info("Approval {} rejected by user {}", currentApproval.getId(), currentUserId);
        return approvalMapper.toResponse(currentApproval);
    }

    @Override
    @Transactional
    public ApprovalResponse sendBack(SendBackRequest request) {
        Approval currentApproval = getCurrentApprovalOrThrow(request.getApprovalId());
        WorkflowInstance instance = currentApproval.getWorkflowInstance();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        validateApprover(currentApproval, currentUserId);

        closeOutApproval(currentApproval, Approval.ApprovalAction.SENT_BACK, request.getComments(), request.getAttachment());

        workflowInstanceRepository.deactivateInstance(instance.getId());
        instance.setCurrentStep(null);
        instance.setCurrentApprover(null);
        workflowInstanceRepository.save(instance);

        Document document = instance.getDocument();
        document.setWorkflowStatus("DRAFT");
        document.setWorkflowInstanceId(null);
        documentRepository.save(document);

        log.info("Approval {} sent back by user {}", currentApproval.getId(), currentUserId);
        return approvalMapper.toResponse(currentApproval);
    }

    @Override
    public ApprovalResponse getCurrentApproval(Long instanceId) {
        Approval approval = approvalRepository.findByWorkflowInstanceIdAndIsCurrentTrue(instanceId)
                .orElseThrow(() -> new WorkflowException("No current approval found for instance: " + instanceId));
        return approvalMapper.toResponse(approval);
    }

    @Override
    public Page<ApprovalResponse> getApprovalHistory(Long instanceId, Pageable pageable) {
        List<Approval> approvals = approvalRepository.findByWorkflowInstanceIdOrderByPerformedAtDesc(instanceId);
        List<ApprovalResponse> responses = approvalMapper.toResponseList(approvals);

        int start = Math.min((int) pageable.getOffset(), responses.size());
        int end = Math.min(start + pageable.getPageSize(), responses.size());

        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    @Override
    @Transactional
    public void escalateApproval(Long instanceId, String reason, Long toUserId) {
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new WorkflowException("Workflow instance not found with id: " + instanceId));

        User fromUser = instance.getCurrentApprover();
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new WorkflowException("Escalation target user not found: " + toUserId));

        Escalation escalation = new Escalation();
        escalation.setWorkflowInstance(instance);
        escalation.setFromStep(instance.getCurrentStep());
        escalation.setFromUser(fromUser);
        escalation.setToUser(toUser);
        escalation.setReason(reason);
        escalation.setEscalatedAt(LocalDateTime.now());
        escalationRepository.save(escalation);

        instance.setCurrentApprover(toUser);
        workflowInstanceRepository.save(instance);

        approvalRepository.findByWorkflowInstanceIdAndIsCurrentTrue(instanceId).ifPresent(currentApproval -> {
            currentApproval.setApprover(toUser);
            approvalRepository.save(currentApproval);
        });

        log.info("Workflow instance {} escalated from user {} to user {}",
                instanceId, fromUser != null ? fromUser.getId() : null, toUserId);

        // TODO: wire to the notification service (e.g. email/in-app) once available to alert the new approver.
    }

    private void closeOutApproval(Approval approval, Approval.ApprovalAction action, String comments, MultipartFile attachment) {
        approval.setAction(action);
        approval.setComments(comments);
        approval.setIsCurrent(false);
        approval.setPerformedAt(LocalDateTime.now());
        if (attachment != null && !attachment.isEmpty()) {
            approval.setAttachmentPath(storeAttachment(attachment));
        }
        approvalRepository.save(approval);
    }

    private Approval getCurrentApprovalOrThrow(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new WorkflowException("Approval not found with id: " + approvalId));
        if (!Boolean.TRUE.equals(approval.getIsCurrent())) {
            throw new WorkflowException("Approval is no longer active/current");
        }
        return approval;
    }

    private void validateApprover(Approval approval, Long currentUserId) {
        if (approval.getApprover() == null || !approval.getApprover().getId().equals(currentUserId)) {
            throw new WorkflowException("Only the assigned approver can act on this approval");
        }
    }

    private String storeAttachment(MultipartFile file) {
        // Placeholder: wire this to the same FileStorageService used for document uploads in Phase 3.
        return "/attachments/approvals/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
    }
}