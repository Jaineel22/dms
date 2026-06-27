package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.ErrorConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.UserCreateRequest;
import com.dms.dto.request.UserUpdateRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.UserResponse;
import com.dms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiConstants.USERS_BASE)
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "CRUD operations for users — ADMIN only except change-password")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email or Employee ID already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {

        UserResponse created = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }
    @Operation(summary = "Update user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PutMapping("/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {

        UserResponse updated = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    // ─── DELETE /users/{userId} ───────────────────────────────────────────────

    @Operation(summary = "Soft-delete user (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Long userId) {

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get user by ID (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }


    @Operation(summary = "List all users — paginated (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User list returned")
    })
    @GetMapping
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0")  int page,
            @Parameter(description = "Page size")             @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field,direction")  @RequestParam(defaultValue = "id,asc") String sort,
            @Parameter(description = "Search keyword")        @RequestParam(required = false) String search) {

        Pageable pageable = buildPageable(page, size, sort);
        Page<UserResponse> result = StringUtils.hasText(search)
                ? userService.searchUsers(search, pageable)
                : userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success(result));
    }


    @Operation(summary = "Toggle user active/inactive status (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status toggled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{userId}/toggle-status")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        userService.toggleUserStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("User status toggled successfully"));
    }

    @Operation(summary = "Change user password (ADMIN or self)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Current password incorrect"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not authorised")
    })
    @PostMapping("/{userId}/change-password")
    @PreAuthorize("@securityUtils.isAdminOrSelf(#userId)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "ID of the user") @PathVariable Long userId,
            @RequestBody ChangePasswordRequest request) {

        userService.changePassword(userId, request.oldPassword(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @Operation(summary = "Reset user password to a temporary value (ADMIN only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{userId}/reset-password")
    @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {

        String temporaryPassword = userService.resetPassword(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully",
                        Map.of("temporaryPassword", temporaryPassword)));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        // Clamp page size to prevent abuse
        int safeSize = Math.min(size, 100);
        String[] parts = sort.split(",");
        String  field     = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, safeSize, Sort.by(dir, field));
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Old password is required")
            String oldPassword,

            @NotBlank(message = "New password is required")
            @Size(min = 8, message = "New password must be at least 8 characters")
            String newPassword
    ) {}
}