package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.AssignManagerRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.HierarchyResponse;
import com.dms.dto.response.TeamMemberResponse;
import com.dms.service.HierarchyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiConstants.HIERARCHY_BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Hierarchy Management", description = "Manage employee reporting structure")
public class HierarchyController {

    private final HierarchyService hierarchyService;

    // ─── POST /hierarchy/assign ───────────────────────────────────────────────

    @Operation(summary = "Assign manager to employee (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Manager assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or Manager not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Circular reference or already has manager"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/assign")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<HierarchyResponse>> assignManager(
            @Valid @RequestBody AssignManagerRequest request) {

        HierarchyResponse response = hierarchyService.assignManager(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Manager assigned successfully", response));
    }

    // ─── PUT /hierarchy/{userId} ──────────────────────────────────────────────

    @Operation(summary = "Update manager for employee (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Manager updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or Manager not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Circular reference")
    })
    @PutMapping("/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<HierarchyResponse>> updateManager(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @RequestParam Long managerId,
            @RequestParam(required = false) String reason) {

        HierarchyResponse response = hierarchyService.updateManager(userId, managerId, reason);
        return ResponseEntity.ok(ApiResponse.success("Manager updated successfully", response));
    }

    // ─── DELETE /hierarchy/{userId} ───────────────────────────────────────────

    @Operation(summary = "Remove manager from employee (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Manager removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> removeManager(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @RequestParam(required = false) String reason) {

        hierarchyService.removeManager(userId, reason);
        return ResponseEntity.noContent().build();
    }

    // ─── GET /hierarchy/{userId} ──────────────────────────────────────────────

    @Operation(summary = "Get reporting hierarchy for a user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporting hierarchy returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<HierarchyResponse>> getReportingHierarchy(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        HierarchyResponse response = hierarchyService.getReportingHierarchy(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /hierarchy/team/{managerId} ──────────────────────────────────────

    @Operation(summary = "Get team members under a manager (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Team members returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Manager not found")
    })
    @GetMapping("/team/{managerId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(
            @Parameter(description = "ID of the manager") @PathVariable Long managerId) {

        List<TeamMemberResponse> response = hierarchyService.getTeamMembers(managerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /hierarchy/reports/{managerId} ───────────────────────────────────

    @Operation(summary = "Get direct reports of a manager (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Direct reports returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Manager not found")
    })
    @GetMapping("/reports/{managerId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getDirectReports(
            @Parameter(description = "ID of the manager") @PathVariable Long managerId) {

        List<TeamMemberResponse> response = hierarchyService.getDirectReports(managerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── GET /hierarchy/chain/{userId} ────────────────────────────────────────

    @Operation(summary = "Get complete reporting chain for a user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reporting chain returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/chain/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<List<HierarchyResponse>>> getReportingChain(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        List<HierarchyResponse> response = hierarchyService.getReportingChain(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}