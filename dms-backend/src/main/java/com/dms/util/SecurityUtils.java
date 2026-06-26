package com.dms.util;

import com.dms.constant.RoleConstants;
import com.dms.entity.User;
import com.dms.exception.ResourceNotFoundException;
import com.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Spring-managed security utility bean.
 *
 * <p>Registered under the name {@code "securityUtils"} so it can be
 * referenced directly in {@code @PreAuthorize} SpEL expressions:</p>
 * <pre>
 *   &#64;PreAuthorize("@securityUtils.isAdminOrSelf(#userId)")
 * </pre>
 */
@Component("securityUtils")
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    // ─── Current-user resolution ──────────────────────────────────────────────

    /**
     * Returns the full {@link User} entity for the currently authenticated caller.
     *
     * @throws ResourceNotFoundException if the email in the security context has no DB match
     * @throws IllegalStateException     if there is no authenticated user in the context
     */
    public User getCurrentUser() {
        String email = getCurrentEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Returns the primary key of the currently authenticated user.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Returns the email (Spring Security username) of the currently authenticated user.
     *
     * @throws IllegalStateException if no authenticated user is present
     */
    public String getCurrentEmail() {
        Authentication auth = getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        return principal.toString();
    }

    // ─── Role checks ──────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the current caller holds the supplied authority
     * string (e.g. {@code "ROLE_ADMIN"}).
     *
     * @param roleName full authority string including the {@code ROLE_} prefix
     */
    public boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleName));
    }

    /**
     * Returns {@code true} if the caller holds {@code ROLE_ADMIN}.
     */
    public boolean isAdmin() {
        return hasRole(RoleConstants.ROLE_ADMIN);
    }

    /**
     * Returns {@code true} if the caller holds {@code ROLE_USER}.
     */
    public boolean isUser() {
        return hasRole(RoleConstants.ROLE_USER);
    }

    /**
     * Returns {@code true} if the caller is an ADMIN, or if the caller's own
     * user ID equals the supplied {@code userId}.
     *
     * <p>Designed for {@code @PreAuthorize} usage:</p>
     * <pre>
     *   &#64;PreAuthorize("@securityUtils.isAdminOrSelf(#userId)")
     * </pre>
     *
     * @param userId the target user ID to check ownership against
     */
    public boolean isAdminOrSelf(Long userId) {
        if (isAdmin()) {
            return true;
        }
        try {
            return getCurrentUserId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException(
                    "No authenticated user present in the security context");
        }
        return auth;
    }
}