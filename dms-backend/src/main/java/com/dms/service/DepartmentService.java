package com.dms.service;

import com.dms.dto.request.DepartmentRequest;
import com.dms.dto.response.DepartmentResponse;
import com.dms.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {

    /**
     * Creates a new department. Only callable by ADMIN.
     *
     * @param request create payload
     * @return created department details (userCount = 0)
     */
    DepartmentResponse createDepartment(DepartmentRequest request);

    /**
     * Updates an existing department. Only callable by ADMIN.
     *
     * @param deptId  target department ID
     * @param request update payload
     * @return updated department details with current user count
     */
    DepartmentResponse updateDepartment(Long deptId, DepartmentRequest request);

    /**
     * Soft-deletes a department (sets {@code isActive = false}).
     * Throws {@link com.dms.exception.BusinessException} if the department
     * still has active users assigned. Only callable by ADMIN.
     *
     * @param deptId target department ID
     */
    void deleteDepartment(Long deptId);

    /**
     * Returns a single department by primary key, including its active user count.
     * Accessible by both ADMIN and USER roles.
     *
     * @param deptId target department ID
     * @return department details with user count
     */
    DepartmentResponse getDepartmentById(Long deptId);

    /**
     * Returns all active departments ordered by name (non-paginated).
     * Useful for dropdown lists in the UI.
     *
     * @return list of all active departments with user counts
     */
    List<DepartmentResponse> getAllDepartments();

    /**
     * Returns a paginated list of active departments.
     * Accessible by both ADMIN and USER roles.
     *
     * @param pageable pagination and sorting parameters
     * @return page of department details with user counts
     */
    Page<DepartmentResponse> getAllDepartments(Pageable pageable);

    /**
     * Returns all active users assigned to the given department.
     * Only callable by ADMIN.
     *
     * @param deptId target department ID
     * @return list of active user profiles in the department
     */
    List<UserResponse> getDepartmentUsers(Long deptId);

    /**
     * Returns {@code true} if no active users are currently assigned
     * to this department (prerequisite for deletion).
     *
     * @param deptId target department ID
     * @return {@code true} if the department has no active users
     */
    Boolean isDepartmentEmpty(Long deptId);
}