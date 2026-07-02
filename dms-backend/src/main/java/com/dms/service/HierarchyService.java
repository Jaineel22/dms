package com.dms.service;

import com.dms.dto.request.AssignManagerRequest;
import com.dms.dto.response.HierarchyResponse;
import com.dms.dto.response.TeamMemberResponse;

import java.util.List;

public interface HierarchyService {

    /**
     * Assigns a manager to a user who currently has no manager.
     * Validates: user exists, manager exists, no self-assignment,
     * manager level > user level, no circular reference.
     */
    HierarchyResponse assignManager(AssignManagerRequest request);

    /**
     * Updates the manager of a user who already has a manager assigned.
     *
     * @param userId       the user whose manager is being changed
     * @param newManagerId the new manager's user ID
     * @param reason       optional audit reason for the change
     */
    HierarchyResponse updateManager(Long userId, Long newManagerId, String reason);

    /**
     * Removes the manager assignment from a user.
     *
     * @param userId the user whose manager assignment is being removed
     * @param reason optional audit reason
     */
    void removeManager(Long userId, String reason);

    /**
     * Returns the hierarchy entry (user + their manager details) for a single user.
     */
    HierarchyResponse getReportingHierarchy(Long userId);

    /**
     * Returns all active users across the entire subtree rooted at managerId
     * (direct reports + their direct reports, recursively).
     */
    List<TeamMemberResponse> getTeamMembers(Long managerId);

    /**
     * Returns only the immediate (level-1) direct reports of a manager.
     */
    List<TeamMemberResponse> getDirectReports(Long managerId);

    /**
     * Returns the full upward reporting chain for a user,
     * from the user themselves up to the root (no manager).
     * Ordered from user → direct manager → grand-manager → … → root.
     */
    List<HierarchyResponse> getReportingChain(Long userId);
}