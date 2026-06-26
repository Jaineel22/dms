package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.DepartmentRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.DepartmentResponse;
import com.dms.dto.response.UserResponse;
import com.dms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.DEPARTMENTS_BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Department Management", description = "CRUD operations for departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    // ─── POST /departments ────────────────────────────────────────────────────

    @Operation(summary = "Create department (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Department created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Name or code already exists")
    })
    @PostMapping
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        DepartmentResponse created = departmentService.createDepartment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", created));
    }

    // ─── PUT /departments/{deptId} ────────────────────────────────────────────

    @Operation(summary = "Update department (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Name or code already in use")
    })
    @PutMapping("/{deptId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @Parameter(description = "ID of the department") @PathVariable Long deptId,
            @Valid @RequestBody DepartmentRequest request) {

        DepartmentResponse updated = departmentService.updateDepartment(deptId, request);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    // ─── DELETE /departments/{deptId} ─────────────────────────────────────────

    @Operation(summary = "Delete department (ADMIN only — blocked if users exist)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Department deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Department has active users assigned")
    })
    @DeleteMapping("/{deptId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "ID of the department") @PathVariable Long deptId) {

        departmentService.deleteDepartment(deptId);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /departments/{deptId} ────────────────────────────────────────────

    @Operation(summary = "Get department by ID (ADMIN, USER)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{deptId}")
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @Parameter(description = "ID of the department") @PathVariable Long deptId) {

        DepartmentResponse department = departmentService.getDepartmentById(deptId);
        return ResponseEntity.ok(ApiResponse.success(department));
    }

    // ─── GET /departments ─────────────────────────────────────────────────────

    @Operation(summary = "List all departments — paginated (ADMIN, USER)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department list returned")
    })
    @GetMapping
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> getAllDepartments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0")       int page,
            @Parameter(description = "Page size")             @RequestParam(defaultValue = "20")      int size,
            @Parameter(description = "Sort field,direction")  @RequestParam(defaultValue = "name,asc") String sort) {

        Pageable pageable = buildPageable(page, size, sort);
        Page<DepartmentResponse> result = departmentService.getAllDepartments(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ─── GET /departments/{deptId}/users ─────────────────────────────────────

    @Operation(summary = "Get all users in a department (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User list returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    @GetMapping("/{deptId}/users")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDepartmentUsers(
            @Parameter(description = "ID of the department") @PathVariable Long deptId) {

        List<UserResponse> users = departmentService.getDepartmentUsers(deptId);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Pageable buildPageable(int page, int size, String sort) {
        int safeSize = Math.min(size, 100);
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, safeSize, Sort.by(dir, field));
    }
}