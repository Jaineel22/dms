package com.dms.service;

import com.dms.dto.request.UserCreateRequest;
import com.dms.dto.request.UserUpdateRequest;
import com.dms.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    /**
     * Creates a new user. Only callable by ADMIN.
     * Encodes password, assigns role and department, validates uniqueness.
     *
     * @param request create payload
     * @return created user profile
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * Updates an existing user. Only callable by ADMIN.
     * Null fields in the request are ignored (patch semantics).
     *
     * @param userId  target user ID
     * @param request update payload
     * @return updated user profile
     */
    UserResponse updateUser(Long userId, UserUpdateRequest request);

    /**
     * Soft-deletes a user (sets {@code isActive = false}). Only callable by ADMIN.
     *
     * @param userId target user ID
     */
    void deleteUser(Long userId);

    /**
     * Returns a single user by primary key. Only callable by ADMIN.
     *
     * @param userId target user ID
     * @return user profile
     */
    UserResponse getUserById(Long userId);

    /**
     * Returns a paginated list of all active users.
     *
     * @param pageable pagination and sorting parameters
     * @return page of user profiles
     */
    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Returns paginated users whose firstName, lastName, email, or employeeId
     * contain the given search term (case-insensitive).
     *
     * @param search  keyword to search for
     * @param pageable pagination and sorting parameters
     * @return page of matching user profiles
     */
    Page<UserResponse> searchUsers(String search, Pageable pageable);

    /**
     * Toggles the {@code isActive} flag of the target user. Only callable by ADMIN.
     *
     * @param userId target user ID
     */
    void toggleUserStatus(Long userId);

    /**
     * Changes the password for a user.
     * ADMIN callers may change any user's password without supplying the old one.
     * Non-ADMIN callers must supply the correct current password and may only
     * change their own password.
     *
     * @param userId      target user ID
     * @param oldPassword current password (validated for non-ADMIN callers)
     * @param newPassword new password (will be BCrypt-encoded)
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Resets a user's password to a system-generated temporary value.
     * Only callable by ADMIN.
     *
     * @param userId target user ID
     * @return the plain-text temporary password (caller must deliver it securely)
     */
    String resetPassword(Long userId);
}