package com.dms.service.impl;

import com.dms.dto.request.DepartmentRequest;
import com.dms.dto.response.DepartmentResponse;
import com.dms.dto.response.UserResponse;
import com.dms.entity.Department;
import com.dms.exception.BusinessException;
import com.dms.exception.DuplicateResourceException;
import com.dms.exception.ResourceNotFoundException;
import com.dms.mapper.DepartmentMapper;
import com.dms.mapper.UserMapper;
import com.dms.repository.DepartmentRepository;
import com.dms.repository.UserRepository;
import com.dms.service.DepartmentService;
import com.dms.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository       userRepository;
    private final DepartmentMapper     departmentMapper;
    private final UserMapper           userMapper;
    private final SecurityUtils        securityUtils;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        Department department = departmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);

        log.info("Admin [{}] created department [{}]",
                securityUtils.getCurrentEmail(), saved.getName());

        return departmentMapper.toResponse(saved, 0L);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long deptId, DepartmentRequest request) {
        Department department = resolveDepartment(deptId);

        if (departmentRepository.existsByNameAndIdNot(request.getName(), deptId)) {
            throw new DuplicateResourceException("Department", "name", request.getName());
        }
        if (departmentRepository.existsByCodeAndIdNot(request.getCode(), deptId)) {
            throw new DuplicateResourceException("Department", "code", request.getCode());
        }

        departmentMapper.updateEntityFromRequest(request, department);
        Department updated = departmentRepository.save(department);

        Long userCount = departmentRepository.countActiveUsersByDepartmentId(deptId);

        log.info("Admin [{}] updated department [{}]",
                securityUtils.getCurrentEmail(), updated.getName());

        return departmentMapper.toResponse(updated, userCount);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteDepartment(Long deptId) {
        Department department = resolveDepartment(deptId);

        if (!isDepartmentEmpty(deptId)) {
            throw new BusinessException(
                    "Cannot delete department '" + department.getName()
                  + "' because it still has active users assigned. "
                  + "Reassign or deactivate all users first.");
        }

        department.setIsActive(false);
        departmentRepository.save(department);

        log.info("Admin [{}] soft-deleted department [{}]",
                securityUtils.getCurrentEmail(), department.getName());
    }

    // ─── Read — single ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long deptId) {
        Department department = resolveDepartment(deptId);
        Long userCount = departmentRepository.countActiveUsersByDepartmentId(deptId);
        return departmentMapper.toResponse(department, userCount);
    }

    // ─── Read — list ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsActive()))
                .map(d -> departmentMapper.toResponse(
                        d,
                        departmentRepository.countActiveUsersByDepartmentId(d.getId())))
                .collect(Collectors.toList());
    }

    // ─── Read — paginated ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAllByIsActiveTrue(pageable)
                .map(d -> departmentMapper.toResponse(
                        d,
                        departmentRepository.countActiveUsersByDepartmentId(d.getId())));
    }

    // ─── Department users ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getDepartmentUsers(Long deptId) {
        resolveDepartment(deptId); // validate existence
        return userRepository.findByDepartmentId(deptId).stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ─── Guard ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Boolean isDepartmentEmpty(Long deptId) {
        return !userRepository.existsByDepartmentIdAndIsActiveTrue(deptId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Department resolveDepartment(Long deptId) {
        return departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", deptId));
    }
}