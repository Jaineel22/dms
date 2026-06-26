package com.dms.constant;

/**
 * Role name constants and ready-made SpEL expressions for {@code @PreAuthorize}.
 *
 * <p>Spring Security stores and matches authorities with the {@code ROLE_} prefix
 * when using {@code hasRole()}. The {@code hasRole('ADMIN')} SpEL expression
 * internally matches against the authority {@code "ROLE_ADMIN"}.</p>
 */
public final class RoleConstants {

    private RoleConstants() {}

    // ─── Full authority strings (stored in DB, seeded, and placed in JWT claims) ─

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER  = "ROLE_USER";

    // ─── Short names (Spring strips "ROLE_" prefix for hasRole() checks) ──────

    public static final String ADMIN = "ADMIN";
    public static final String USER  = "USER";

    // ─── Ready-made SpEL expressions for @PreAuthorize ────────────────────────

    /** Restricts access to ADMIN role only. */
    public static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";

    /** Requires any authenticated user (ADMIN or USER). */
    public static final String IS_AUTHENTICATED = "isAuthenticated()";

    /** Allows either ADMIN or USER role. */
    public static final String HAS_ANY_ROLE = "hasAnyRole('ADMIN', 'USER')";
}