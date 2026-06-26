package com.dms.constant;

/**
 * Central registry of all API path constants.
 *
 * <p>Note: {@code server.servlet.context-path} in {@code application.yml} is NOT set,
 * so Spring Boot serves the API directly at the paths defined here.
 * The Swagger base URL is therefore {@code /v3/api-docs} and all endpoints are
 * accessible at the paths below without a prefix.</p>
 */
public final class ApiConstants {

    private ApiConstants() {}

    // ─── Versioning ───────────────────────────────────────────────────────────

    public static final String API_VERSION  = "/api/v1";

    // ─── Module bases ─────────────────────────────────────────────────────────

    public static final String AUTH_BASE        = API_VERSION + "/auth";
    public static final String USERS_BASE       = API_VERSION + "/users";
    public static final String DEPARTMENTS_BASE = API_VERSION + "/departments";

    // ─── Auth endpoints ───────────────────────────────────────────────────────

    public static final String AUTH_LOGIN   = AUTH_BASE + "/login";
    public static final String AUTH_ME      = AUTH_BASE + "/me";
    public static final String AUTH_LOGOUT  = AUTH_BASE + "/logout";

    // ─── User endpoints ───────────────────────────────────────────────────────

    public static final String USER_BY_ID           = USERS_BASE + "/{userId}";
    public static final String USER_TOGGLE_STATUS   = USERS_BASE + "/{userId}/toggle-status";
    public static final String USER_CHANGE_PASSWORD = USERS_BASE + "/{userId}/change-password";
    public static final String USER_RESET_PASSWORD  = USERS_BASE + "/{userId}/reset-password";

    // ─── Department endpoints ─────────────────────────────────────────────────

    public static final String DEPT_BY_ID    = DEPARTMENTS_BASE + "/{deptId}";
    public static final String DEPT_USERS    = DEPARTMENTS_BASE + "/{deptId}/users";

    // ─── Public (permit-all) matchers — used in SecurityConfig ───────────────

    public static final String[] PUBLIC_ENDPOINTS = {
            AUTH_LOGIN,
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };
}