package com.dms.service.impl;

import com.dms.dto.request.ApproveRequest;
import com.dms.dto.request.RejectRequest;
import com.dms.dto.request.SendBackRequest;
import com.dms.dto.response.ApprovalResponse;
import com.dms.entity.Approval;
import com.dms.entity.Document;
import com.dms.entity.Escalation;
import com.dms.entity.Notification;
import com.dms.entity.User;
import com.dms.entity.WorkflowInstance;
import com.dms.entity.WorkflowStep;
import com.dms.exception.AccessDeniedException;
import com.dms.exception.ConflictException;
import com.dms.exception.WorkflowException;
import com.dms.mapper.ApprovalMapper;
import com.dms.repository.ApprovalRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.EscalationRepository;
import com.dms.repository.UserRepository;
import com.dms.repository.WorkflowInstanceRepository;
import com.dms.repository.WorkflowStepRepository;
import com.dms.util.SecurityUtils;
import com.dms.service.ApprovalService;
import com.dms.service.AuditService;
import com.dms.service.HierarchyService;
import com.dms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final ApprovalMapper approvalMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ApprovalResponse approve(ApproveRequest request) {
        Approval currentApproval = getCurrentApprovalOrThrow(request.getApprovalId());
        WorkflowInstance instance = currentApproval.getWorkflowInstance();
        Long currentUserId = securityUtils.getCurrentUserId();
        validateApprover(currentApproval, currentUserId);

        closeOutApproval(currentApproval, Approval.ApprovalAction.APPROVED, request.getComments(), request.getAttachment());

        WorkflowStep nextStep = workflowStepRepository
                .findByWorkflowDefinitionIdAndStepNumber(
                        instance.getWorkflowDefinition().getId(),
                        currentApproval.getStep().getStepNumber() + 1)
                .orElse(null);

        Document document = instance.getDocument();
        User nextApproverToNotify = null;

        if (nextStep == null) {
            instance.setStatus(WorkflowInstance.WorkflowInstanceStatus.APPROVED);
            instance.setCompletedAt(LocalDateTime.now());
            instance.setCurrentStep(null);
            instance.setCurrentApprover(null);

            document.setStatus("APPROVED");
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
            nextApproverToNotify = nextApprover;
        }

        workflowInstanceRepository.save(instance);
        documentRepository.save(document);

        log.info("Approval {} approved by user {}", currentApproval.getId(), currentUserId);
        ApprovalResponse response = approvalMapper.toResponse(currentApproval);

        String docLink = "/documents/" + document.getId();
        if (nextApproverToNotify != null) {
            safeNotify(nextApproverToNotify.getId(), Notification.Type.APPROVAL_REQUIRED,
                    "Approval required",
                    "You have a new approval request for \"" + document.getTitle() + "\".",
                    docLink);
        } else {
            safeNotify(documentOwnerId(document), Notification.Type.APPROVED,
                    "Document approved",
                    "Your document \"" + document.getTitle() + "\" has been fully approved.",
                    docLink);
        }
        safeAudit("APPROVAL", currentApproval.getId(), "APPROVE", null, response);

        return response;
    }

    @Override
    @Transactional
    public ApprovalResponse reject(RejectRequest request) {
        Approval currentApproval = getCurrentApprovalOrThrow(request.getApprovalId());
        WorkflowInstance instance = currentApproval.getWorkflowInstance();
        Long currentUserId = securityUtils.getCurrentUserId();
        validateApprover(currentApproval, currentUserId);

        closeOutApproval(currentApproval, Approval.ApprovalAction.REJECTED, request.getComments(), request.getAttachment());

        instance.setStatus(WorkflowInstance.WorkflowInstanceStatus.REJECTED);
        instance.setCompletedAt(LocalDateTime.now());
        instance.setCurrentStep(null);
        instance.setCurrentApprover(null);
        workflowInstanceRepository.save(instance);

        Document document = instance.getDocument();
        document.setStatus("REJECTED");
        documentRepository.save(document);

        log.info("Approval {} rejected by user {}", currentApproval.getId(), currentUserId);
        ApprovalResponse response = approvalMapper.toResponse(currentApproval);

        safeNotify(documentOwnerId(document), Notification.Type.REJECTED,
                "Document rejected",
                "Your document \"" + document.getTitle() + "\" was rejected."
                        + commentSuffix(request.getComments()),
                "/documents/" + document.getId());
        safeAudit("APPROVAL", currentApproval.getId(), "REJECT", null, response);

        return response;
    }

    @Override
    @Transactional
    public ApprovalResponse sendBack(SendBackRequest request) {
        Approval currentApproval = getCurrentApprovalOrThrow(request.getApprovalId());
        WorkflowInstance instance = currentApproval.getWorkflowInstance();
        Long currentUserId = securityUtils.getCurrentUserId();
        validateApprover(currentApproval, currentUserId);

        closeOutApproval(currentApproval, Approval.ApprovalAction.SENT_BACK, request.getComments(), request.getAttachment());

        workflowInstanceRepository.deactivateInstance(instance.getId());
        instance.setCurrentStep(null);
        instance.setCurrentApprover(null);
        workflowInstanceRepository.save(instance);

        Document document = instance.getDocument();
        document.setStatus("DRAFT");
        documentRepository.save(document);

        log.info("Approval {} sent back by user {}", currentApproval.getId(), currentUserId);
        ApprovalResponse response = approvalMapper.toResponse(currentApproval);

        safeNotify(documentOwnerId(document), Notification.Type.SENT_BACK,
                "Document sent back",
                "Your document \"" + document.getTitle() + "\" was sent back for changes."
                        + commentSuffix(request.getComments()),
                "/documents/" + document.getId());
        safeAudit("APPROVAL", currentApproval.getId(), "SEND_BACK", null, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalResponse getCurrentApproval(Long instanceId) {
        Approval approval = approvalRepository.findByWorkflowInstanceIdAndIsCurrentTrue(instanceId)
                .orElseThrow(() -> new WorkflowException("No current approval found for instance: " + instanceId));
        return approvalMapper.toResponse(approval);
    }

    @Override
    @Transactional(readOnly = true)
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
        if (!Boolean.TRUE.equals(toUser.getIsActive())) {
            throw new WorkflowException("Escalation target user is not active: " + toUserId);
        }
        if (fromUser != null && fromUser.getId().equals(toUserId)) {
            throw new WorkflowException("Cannot escalate an approval to its current approver");
        }

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

        Long currentApprovalId = approvalRepository.findByWorkflowInstanceIdAndIsCurrentTrue(instanceId)
                .map(currentApproval -> {
                    currentApproval.setApprover(toUser);
                    approvalRepository.save(currentApproval);
                    return currentApproval.getId();
                })
                .orElse(null);

        log.info("Workflow instance {} escalated from user {} to user {}",
                instanceId, fromUser != null ? fromUser.getId() : null, toUserId);

        Document document = instance.getDocument();
        safeNotify(toUser.getId(), Notification.Type.ESCALATED,
                "Approval escalated to you",
                "An approval for \"" + document.getTitle() + "\" has been escalated to you."
                        + commentSuffix(reason),
                "/documents/" + document.getId());

        Map<String, Object> newValue = new LinkedHashMap<>();
        newValue.put("instanceId", instanceId);
        newValue.put("fromUserId", fromUser != null ? fromUser.getId() : null);
        newValue.put("toUserId", toUserId);
        newValue.put("reason", reason);
        safeAudit("APPROVAL", currentApprovalId != null ? currentApprovalId : instanceId,
                "ESCALATE", null, newValue);
    }

    private Long documentOwnerId(Document document) {
        return document.getOwner() != null ? document.getOwner().getId() : null;
    }

    private String commentSuffix(String comments) {
        return (comments != null && !comments.isBlank()) ? " Comment: " + comments : "";
    }

    /** Best-effort notification: a delivery failure must never break the business transaction. */
    private void safeNotify(Long userId, String type, String title, String message, String link) {
        if (userId == null) {
            return;
        }
        try {
            notificationService.createNotificationForUser(userId, type, title, message, link);
        } catch (Exception e) {
            log.warn("Failed to send {} notification to user {}: {}", type, userId, e.getMessage());
        }
    }

    /** Best-effort audit: a logging failure must never break the business transaction. */
    private void safeAudit(String entityType, Long entityId, String action, Object oldValue, Object newValue) {
        try {
            auditService.logAction(entityType, entityId, action, oldValue, newValue);
        } catch (Exception e) {
            log.warn("Failed to write audit log for {} {}:{}: {}", action, entityType, entityId, e.getMessage());
        }
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
            throw new ConflictException("Approval is no longer active/current");
        }
        return approval;
    }

    private void validateApprover(Approval approval, Long currentUserId) {
        if (approval.getApprover() == null || !approval.getApprover().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the assigned approver can act on this approval");
        }
    }

    private String storeAttachment(MultipartFile file) {
        // Placeholder: wire this to the same FileStorageService used for document uploads in Phase 3.
        return "/attachments/approvals/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
    }
}