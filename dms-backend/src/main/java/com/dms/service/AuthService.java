package com.dms.service;

import com.dms.dto.request.LoginRequest;
import com.dms.dto.response.AuthResponse;
import com.dms.dto.response.UserResponse;

public interface AuthService {

    /**
     * Authenticates a user with email and password.
     * Handles login-attempt tracking and account lockout.
     *
     * @param request login credentials
     * @return {@link AuthResponse} containing the JWT token and user profile
     */
    AuthResponse login(LoginRequest request);

    /**
     * Returns the profile of the currently authenticated user,
     * resolved from the active Spring Security context.
     *
     * @return {@link UserResponse} for the caller
     */
    UserResponse getCurrentUser();

    /**
     * Invalidates the supplied token.
     * Stateless JWT — this is a no-op in Phase 1 and a hook for a
     * token-blacklist implementation (e.g. Redis) in future phases.
     *
     * @param token raw Bearer token value (may be null)
     */
    void logout(String token);
}