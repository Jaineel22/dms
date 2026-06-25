package com.dms.mapper;

import com.dms.dto.request.UserCreateRequest;
import com.dms.dto.request.UserUpdateRequest;
import com.dms.dto.response.DepartmentResponse;
import com.dms.dto.response.RoleResponse;
import com.dms.dto.response.UserResponse;
import com.dms.entity.Department;
import com.dms.entity.Role;
import com.dms.entity.User;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    // ─── Entity → Response ────────────────────────────────────────────────────

    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "role",       source = "role")
    @Mapping(target = "department", source = "department")
    UserResponse toResponse(User user);

    // ─── CreateRequest → Entity ───────────────────────────────────────────────

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "isActive",         constant = "true")
    @Mapping(target = "role",             ignore = true)
    @Mapping(target = "department",       ignore = true)
    @Mapping(target = "password",         ignore = true)   // encoded by service
    @Mapping(target = "lastLoginAt",      ignore = true)
    @Mapping(target = "passwordChangedAt",ignore = true)
    @Mapping(target = "loginAttempts",    constant = "0")
    @Mapping(target = "lockedUntil",      ignore = true)
    @Mapping(target = "profileImage",     ignore = true)
    User toEntity(UserCreateRequest request);

    // ─── UpdateRequest → Entity (patch) ──────────────────────────────────────

    /**
     * Applies non-null fields from {@link UserUpdateRequest} onto an existing
     * {@link User} entity. Fields not present in the request are left unchanged
     * (IGNORE strategy). Role and Department are resolved by the service layer.
     */
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "updatedBy",        ignore = true)
    @Mapping(target = "role",             ignore = true)
    @Mapping(target = "department",       ignore = true)
    @Mapping(target = "password",         ignore = true)
    @Mapping(target = "employeeId",       ignore = true)
    @Mapping(target = "lastLoginAt",      ignore = true)
    @Mapping(target = "passwordChangedAt",ignore = true)
    @Mapping(target = "loginAttempts",    ignore = true)
    @Mapping(target = "lockedUntil",      ignore = true)
    @Mapping(target = "profileImage",     ignore = true)
    void updateEntityFromRequest(UserUpdateRequest request, @MappingTarget User user);

    // ─── Role → RoleResponse ─────────────────────────────────────────────────

    RoleResponse toRoleResponse(Role role);

    // ─── Department → DepartmentResponse (no userCount — use DepartmentMapper) ─

    @Mapping(target = "userCount", ignore = true)
    DepartmentResponse toDepartmentResponse(Department department);
}