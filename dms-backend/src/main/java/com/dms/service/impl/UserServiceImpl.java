package com.dms.service.impl;

import com.dms.dto.request.UserCreateRequest;
import com.dms.dto.request.UserUpdateRequest;
import com.dms.dto.response.UserResponse;
import com.dms.entity.Department;
import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.exception.AccessDeniedException;
import com.dms.exception.BusinessException;
import com.dms.exception.DuplicateResourceException;
import com.dms.exception.InvalidCredentialsException;
import com.dms.exception.ResourceNotFoundException;
import com.dms.mapper.UserMapper;
import com.dms.repository.DepartmentRepository;
import com.dms.repository.RoleRepository;
import com.dms.repository.UserRepository;
import com.dms.service.UserService;
import com.dms.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository       userRepository;
    private final RoleRepository       roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper           userMapper;
    private final PasswordEncoder      passwordEncoder;
    private final SecurityUtils        securityUtils;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Uniqueness guards
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("User", "employeeId", request.getEmployeeId());
        }

        Role role = resolveRole(request.getRoleId());
        Department department = resolveDepartment(request.getDepartmentId());

        // Optional manager validation
        if (request.getManagerId() != null) {
            validateManagerExists(request.getManagerId());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setDepartment(department);
        user.setPasswordChangedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        log.info("Admin [{}] created user [{}]", securityUtils.getCurrentEmail(), saved.getEmail());

        return userMapper.toResponse(saved);
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = resolveUser(userId);

        // Uniqueness checks (exclude self)
        if (StringUtils.hasText(request.getEmail())
                && userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Optional manager validation
        if (request.getManagerId() != null) {
            if (request.getManagerId().equals(userId)) {
                throw new BusinessException("A user cannot be their own manager");
            }
            validateManagerExists(request.getManagerId());
        }

        // Apply patch
        userMapper.updateEntityFromRequest(request, user);

        // Resolve role if provided
        if (request.getRoleId() != null) {
            user.setRole(resolveRole(request.getRoleId()));
        }

        // Resolve department if provided
        if (request.getDepartmentId() != null) {
            user.setDepartment(resolveDepartment(request.getDepartmentId()));
        }

        User updated = userRepository.save(user);
        log.info("Admin [{}] updated user [{}]", securityUtils.getCurrentEmail(), updated.getEmail());

        return userMapper.toResponse(updated);
    }

    // ─── Delete (soft) ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = resolveUser(userId);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("Admin [{}] soft-deleted user [{}]", securityUtils.getCurrentEmail(), user.getEmail());
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(resolveUser(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllByIsActiveTrue(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String search, Pageable pageable) {
        return userRepository.searchUsers(search, pageable)
                .map(userMapper::toResponse);
    }

    // ─── Toggle status ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = resolveUser(userId);
        boolean newStatus = !Boolean.TRUE.equals(user.getIsActive());
        user.setIsActive(newStatus);
        userRepository.save(user);
        log.info("Admin [{}] toggled user [{}] status to [{}]",
                securityUtils.getCurrentEmail(), user.getEmail(), newStatus);
    }

    // ─── Change password ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User caller = securityUtils.getCurrentUser();
        User target = resolveUser(userId);

        boolean callerIsAdmin = securityUtils.isAdmin();
        boolean callerIsSelf  = caller.getId().equals(userId);

        if (!callerIsAdmin && !callerIsSelf) {
            throw new AccessDeniedException("You are not authorised to change this user's password");
        }

        // Non-admin callers must supply the correct current password
        if (!callerIsAdmin) {
            if (!passwordEncoder.matches(oldPassword, target.getPassword())) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
        }

        target.setPassword(passwordEncoder.encode(newPassword));
        target.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(target);

        log.info("Password changed for user [{}] by [{}]",
                target.getEmail(), caller.getEmail());
    }

    // ─── Reset password ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public String resetPassword(Long userId) {
        User user = resolveUser(userId);

        String temporaryPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Admin [{}] reset password for user [{}]",
                securityUtils.getCurrentEmail(), user.getEmail());

        return temporaryPassword;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User resolveUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Role resolveRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
    }

    private Department resolveDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));
    }

    private void validateManagerExists(Long managerId) {
        if (!userRepository.existsById(managerId)) {
            throw new ResourceNotFoundException("User (manager)", "id", managerId);
        }
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        // Base64 gives us alphanumeric + symbols — strip padding for cleanliness
        String base = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // Ensure policy compliance: prefix with known upper + digit + special
        return "Tmp1@" + base.substring(0, 10);
    }
}