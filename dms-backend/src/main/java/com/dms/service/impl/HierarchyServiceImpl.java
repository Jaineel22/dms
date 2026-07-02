package com.dms.service.impl;

import com.dms.dto.request.AssignManagerRequest;
import com.dms.dto.response.HierarchyResponse;
import com.dms.dto.response.TeamMemberResponse;
import com.dms.entity.HierarchyHistory;
import com.dms.entity.User;
import com.dms.exception.BusinessException;
import com.dms.exception.ResourceNotFoundException;
import com.dms.repository.HierarchyHistoryRepository;
import com.dms.repository.UserRepository;
import com.dms.service.HierarchyService;
import com.dms.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HierarchyServiceImpl implements HierarchyService {

    /** Maximum depth we will walk up the reporting chain before aborting. */
    private static final int MAX_CHAIN_DEPTH = 20;

    private final UserRepository          userRepository;
    private final HierarchyHistoryRepository historyRepository;
    private final SecurityUtils           securityUtils;

    // ─── Assign manager ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public HierarchyResponse assignManager(AssignManagerRequest request) {
        User user    = resolveUser(request.getUserId());
        User manager = resolveUser(request.getManagerId());

        // Business guards
        if (user.getManagerId() != null) {
            throw new BusinessException(
                "User '" + user.getEmail() + "' already has a manager assigned. " +
                "Use updateManager() to change it.");
        }
        validateManagerAssignment(user, manager, true);

        // Persist
        Long previousManagerId = null; // was null (first assignment)
        user.setManagerId(manager.getId());
        userRepository.save(user);

        recordHistory(user.getId(), previousManagerId, manager.getId(), request.getReason());

        log.info("Manager [{}] assigned to user [{}] by [{}]",
                manager.getEmail(), user.getEmail(), securityUtils.getCurrentEmail());

        return buildHierarchyResponse(user, manager);
    }

    // ─── Update manager ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public HierarchyResponse updateManager(Long userId, Long newManagerId, String reason) {
        User user       = resolveUser(userId);
        User newManager = resolveUser(newManagerId);

        if (user.getManagerId() == null) {
            throw new BusinessException(
                "User '" + user.getEmail() + "' has no manager assigned. " +
                "Use assignManager() to assign one.");
        }
        validateManagerAssignment(user, newManager, false);

        Long previousManagerId = user.getManagerId();
        user.setManagerId(newManager.getId());
        userRepository.save(user);

        recordHistory(user.getId(), previousManagerId, newManager.getId(), reason);

        log.info("Manager updated for user [{}]: [{}] → [{}] by [{}]",
                user.getEmail(), previousManagerId, newManager.getId(),
                securityUtils.getCurrentEmail());

        return buildHierarchyResponse(user, newManager);
    }

    // ─── Remove manager ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeManager(Long userId, String reason) {
        User user = resolveUser(userId);

        if (user.getManagerId() == null) {
            throw new BusinessException(
                "User '" + user.getEmail() + "' has no manager assigned.");
        }

        Long previousManagerId = user.getManagerId();
        user.setManagerId(null);
        userRepository.save(user);

        recordHistory(user.getId(), previousManagerId, null, reason);

        log.info("Manager removed from user [{}] by [{}]",
                user.getEmail(), securityUtils.getCurrentEmail());
    }

    // ─── Get reporting hierarchy (single user + manager details) ─────────────

    @Override
    @Transactional(readOnly = true)
    public HierarchyResponse getReportingHierarchy(Long userId) {
        User user = resolveUser(userId);
        User manager = user.getManagerId() != null
                ? userRepository.findById(user.getManagerId()).orElse(null)
                : null;
        return buildHierarchyResponse(user, manager);
    }

    // ─── Team members (entire subtree) ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long managerId) {
        resolveUser(managerId); // validate manager exists
        List<User> team = new ArrayList<>();
        collectSubtree(managerId, team, new HashSet<>());
        return team.stream()
                .map(this::buildTeamMemberResponse)
                .collect(Collectors.toList());
    }

    // ─── Direct reports (level-1 only) ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getDirectReports(Long managerId) {
        resolveUser(managerId); // validate manager exists
        return userRepository.findByManagerId(managerId).stream()
                .map(this::buildTeamMemberResponse)
                .collect(Collectors.toList());
    }

    // ─── Reporting chain (upward) ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<HierarchyResponse> getReportingChain(Long userId) {
        List<HierarchyResponse> chain = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        User current = resolveUser(userId);

        while (current != null && visited.size() < MAX_CHAIN_DEPTH) {
            if (!visited.add(current.getId())) {
                log.warn("Circular reference detected in reporting chain for user [{}]", userId);
                break;
            }
            User manager = current.getManagerId() != null
                    ? userRepository.findById(current.getManagerId()).orElse(null)
                    : null;
            chain.add(buildHierarchyResponse(current, manager));
            current = manager;
        }

        return chain;
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    /**
     * Central validation for both assign and update operations.
     *
     * @param isAssign true = first-time assign; false = update (reassign)
     */
    private void validateManagerAssignment(User user, User manager, boolean isAssign) {
        // 1. Self-assignment guard
        if (user.getId().equals(manager.getId())) {
            throw new BusinessException("A user cannot be assigned as their own manager.");
        }

        // 2. Employee level guard — manager must have a strictly higher level
        int userLevel    = user.getEmployeeLevel()    != null ? user.getEmployeeLevel()    : 1;
        int managerLevel = manager.getEmployeeLevel() != null ? manager.getEmployeeLevel() : 1;
        if (managerLevel <= userLevel) {
            throw new BusinessException(String.format(
                "Manager's employee level (%d) must be higher than user's employee level (%d). " +
                "Hierarchy: 1=Employee, 2=TeamLead, 3=Manager, 4=Director.",
                managerLevel, userLevel));
        }

        // 3. Circular-reference guard (walk up from proposed manager, ensure user not found)
        detectCircularReference(user.getId(), manager.getId());
    }

    /**
     * Walks up the manager chain starting from {@code proposedManagerId}.
     * Throws if {@code userId} is found anywhere in the chain (circular reference).
     */
    private void detectCircularReference(Long userId, Long proposedManagerId) {
        Set<Long> visited = new HashSet<>();
        Long current = proposedManagerId;
        while (current != null && visited.size() < MAX_CHAIN_DEPTH) {
            if (!visited.add(current)) break; // already seen — existing cycle, stop walking
            if (current.equals(userId)) {
                throw new BusinessException(
                    "Circular reference detected: assigning this manager would create " +
                    "a cycle in the reporting hierarchy.");
            }
            User node = userRepository.findById(current).orElse(null);
            current = (node != null) ? node.getManagerId() : null;
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     * DFS walk collecting all users in the subtree rooted at managerId.
     * Uses visited set to guard against pre-existing data cycles.
     */
    private void collectSubtree(Long managerId, List<User> accumulator, Set<Long> visited) {
        if (!visited.add(managerId)) return;
        List<User> directReports = userRepository.findByManagerId(managerId);
        for (User report : directReports) {
            accumulator.add(report);
            collectSubtree(report.getId(), accumulator, visited);
        }
    }

    private void recordHistory(Long userId, Long previousManagerId, Long newManagerId, String reason) {
        Long changedBy = securityUtils.getCurrentUser().getId();
        HierarchyHistory history = HierarchyHistory.builder()
                .userId(userId)
                .previousManagerId(previousManagerId)
                .newManagerId(newManagerId)
                .changedBy(changedBy)
                .changeReason(reason)
                .build();
        historyRepository.save(history);
    }

    private HierarchyResponse buildHierarchyResponse(User user, User manager) {
        return HierarchyResponse.builder()
                .userId(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .employeeId(user.getEmployeeId())
                .employeeLevel(user.getEmployeeLevel())
                .designation(user.getDesignation())
                .managerId(manager != null ? manager.getId() : null)
                .managerName(manager != null
                        ? manager.getFirstName() + " " + manager.getLastName()
                        : null)
                .managerLevel(manager != null ? manager.getEmployeeLevel() : null)
                .departmentName(user.getDepartment() != null
                        ? user.getDepartment().getName()
                        : null)
                .build();
    }

    private TeamMemberResponse buildTeamMemberResponse(User user) {
        return TeamMemberResponse.builder()
                .userId(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .employeeId(user.getEmployeeId())
                .designation(user.getDesignation())
                .employeeLevel(user.getEmployeeLevel())
                .isActive(user.getIsActive())
                .build();
    }
}