package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.RoleResponse;
import com.dms.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.ROLES_BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Role Management", description = "Read-only access to system roles")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get all roles")
    @GetMapping
    @PreAuthorize(RoleConstants.IS_AUTHENTICATED)
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
}
