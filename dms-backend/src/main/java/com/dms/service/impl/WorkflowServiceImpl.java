package com.dms.service.impl;

import com.dms.dto.request.WorkflowCreateRequest;
import com.dms.dto.request.WorkflowStepRequest;
import com.dms.dto.request.WorkflowUpdateRequest;
import com.dms.dto.response.WorkflowResponse;
import com.dms.entity.Department;
import com.dms.entity.UserWorkflow;
import com.dms.entity.WorkflowDefinition;
import com.dms.entity.WorkflowStep;
import com.dms.exception.BusinessException;
import com.dms.exception.DuplicateResourceException;
import com.dms.exception.ResourceNotFoundException;
import com.dms.mapper.WorkflowMapper;
import com.dms.mapper.WorkflowStepMapper;
import com.dms.repository.DepartmentRepository;
import com.dms.repository.RoleRepository;
import com.dms.repository.UserRepository;
import com.dms.repository.UserWorkflowRepository;
import com.dms.repository.WorkflowDefinitionRepository;
import com.dms.repository.WorkflowStepRepository;
import com.dms.service.WorkflowService;
import com.dms.util.SecurityUtils;
import com.dms.validator.WorkflowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowStepRepository       stepRepository;
    private final UserWorkflowRepository       userWorkflowRepository;
    private final UserRepository               userRepository;
    private final DepartmentRepository         departmentRepository;
    private final RoleRepository               roleRepository;
    private final WorkflowMapper               workflowMapper;
    private final WorkflowStepMapper           stepMapper;
    private final WorkflowValidator            workflowValidator;
    private final SecurityUtils                securityUtils;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkflowResponse createWorkflow(WorkflowCreateRequest request) {
        // Uniqueness check
        if (workflowRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Workflow", "name", request.getName());
        }

        // Validate steps before persisting anything
        workflowValidator.validateWorkflowSteps(request.getSteps());

        // Build entity
        WorkflowDefinition workflow = workflowMapper.toEntity(request);

        // Resolve optional department
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            workflow.setDepartment(dept);
        }

        // Save workflow first (steps need the FK)
        WorkflowDefinition saved = workflowRepository.save(workflow);

        // Build and link steps
        List<WorkflowStep> steps = request.getSteps().stream()
                .map(stepReq -> {
                    WorkflowStep step = stepMapper.toEntity(stepReq);
                    step.setWorkflow(saved);
                    return step;
                })
                .collect(Collectors.toList());

        stepRepository.saveAll(steps);
        saved.setSteps(steps);

        log.info("Workflow '{}' created with {} steps by [{}]",
                saved.getName(), steps.size(), securityUtils.getCurrentEmail());

        return workflowMapper.toResponse(saved);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkflowResponse updateWorkflow(Long workflowId, WorkflowUpdateRequest request) {
        WorkflowDefinition workflow = resolveWorkflow(workflowId);

        // Name uniqueness (exclude self)
        if (StringUtils.hasText(request.getName())
                && workflowRepository.existsByNameAndIdNot(request.getName(), workflowId)) {
            throw new DuplicateResourceException("Workflow", "name", request.getName());
        }

        // Resolve new department if provided
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department", "id", request.getDepartmentId()));
            workflow.setDepartment(dept);
        }

        workflowMapper.updateEntityFromRequest(request, workflow);
        WorkflowDefinition updated = workflowRepository.save(workflow);

        log.info("Workflow [{}] updated by [{}]", workflowId, securityUtils.getCurrentEmail());
        return workflowMapper.toResponse(updated);
    }

    // ─── Delete (soft) ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteWorkflow(Long workflowId) {
        WorkflowDefinition workflow = resolveWorkflow(workflowId);
        workflow.setIsActive(false);
        workflowRepository.save(workflow);
        log.info("Workflow [{}] soft-deleted by [{}]",
                workflowId, securityUtils.getCurrentEmail());
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowById(Long workflowId) {
        return workflowMapper.toResponse(resolveWorkflow(workflowId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkflowResponse> getAllWorkflows(Pageable pageable) {
        return workflowRepository.findAllByIsActiveTrue(pageable)
                .map(workflowMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowResponse> getWorkflowsByDepartment(Long departmentId) {
        return workflowRepository.findByDepartmentIdAndIsActiveTrue(departmentId)
                .stream()
                .map(workflowMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkflowResponse getWorkflowWithSteps(Long workflowId) {
        WorkflowDefinition workflow = workflowRepository.findByIdWithSteps(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow", "id", workflowId));
        return workflowMapper.toResponse(workflow);
    }

    // ─── User-workflow assignment ─────────────────────────────────────────────

    @Override
    @Transactional
    public void assignWorkflowToUser(Long userId, Long workflowId) {
        // Validate both exist
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        WorkflowDefinition workflow = resolveWorkflow(workflowId);

        // Duplicate assignment guard
        if (userWorkflowRepository.existsByUserIdAndWorkflowId(userId, workflowId)) {
            throw new BusinessException(
                "User [" + userId + "] is already assigned to workflow '" + workflow.getName() + "'.");
        }

        UserWorkflow assignment = UserWorkflow.builder()
                .userId(userId)
                .workflow(workflow)
                .isActive(true)
                .assignedAt(LocalDateTime.now())
                .assignedBy(securityUtils.getCurrentUser().getId())
                .build();

        userWorkflowRepository.save(assignment);
        log.info("Workflow [{}] assigned to user [{}] by [{}]",
                workflowId, userId, securityUtils.getCurrentEmail());
    }

    @Override
    @Transactional
    public void removeWorkflowFromUser(Long userId, Long workflowId) {
        UserWorkflow assignment = userWorkflowRepository
                .findByUserIdAndWorkflowId(userId, workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserWorkflow assignment", "userId/workflowId",
                        userId + "/" + workflowId));

        assignment.setIsActive(false);
        userWorkflowRepository.save(assignment);

        log.info("Workflow [{}] removed from user [{}] by [{}]",
                workflowId, userId, securityUtils.getCurrentEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkflowResponse> getUserWorkflows(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return userWorkflowRepository.findByUserIdAndIsActiveTrueOrderByAssignedAtDesc(userId)
                .stream()
                .map(uw -> workflowMapper.toResponse(uw.getWorkflow()))
                .collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private WorkflowDefinition resolveWorkflow(Long workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow", "id", workflowId));
    }
}