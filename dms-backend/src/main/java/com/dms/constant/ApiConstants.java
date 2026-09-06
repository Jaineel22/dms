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
    public static final String ROLES_BASE       = API_VERSION + "/roles";

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

    // ─── Hierarchy endpoints ──────────────────────────────────────────────────

    public static final String HIERARCHY_BASE    = API_VERSION + "/hierarchy";
    public static final String HIERARCHY_ASSIGN  = HIERARCHY_BASE + "/assign";
    public static final String HIERARCHY_USER    = HIERARCHY_BASE + "/{userId}";
    public static final String HIERARCHY_TEAM    = HIERARCHY_BASE + "/team/{managerId}";
    public static final String HIERARCHY_REPORTS = HIERARCHY_BASE + "/reports/{managerId}";
    public static final String HIERARCHY_CHAIN   = HIERARCHY_BASE + "/chain/{userId}";

    // ─── Workflow Definition endpoints (Phase 2) ──────────────────────────────

    public static final String WORKFLOW_BASE       = API_VERSION + "/workflows";
    public static final String WORKFLOW_BY_ID      = WORKFLOW_BASE + "/{workflowId}";
    public static final String WORKFLOW_STEPS      = WORKFLOW_BASE + "/{workflowId}/steps";
    public static final String WORKFLOW_DEPARTMENT = WORKFLOW_BASE + "/department/{departmentId}";
    public static final String WORKFLOW_ASSIGN     = WORKFLOW_BASE + "/{workflowId}/assign/{userId}";
    public static final String WORKFLOW_USER       = WORKFLOW_BASE + "/user/{userId}";

    // ─── Workflow Execution endpoints (Phase 4) ───────────────────────────────

    public static final String WORKFLOW_EXECUTION_BASE = API_VERSION + "/workflow";
    public static final String WORKFLOW_SUBMIT         = WORKFLOW_EXECUTION_BASE + "/submit";
    public static final String WORKFLOW_INSTANCES      = WORKFLOW_EXECUTION_BASE + "/instances";
    public static final String WORKFLOW_INSTANCE_BY_ID = WORKFLOW_INSTANCES + "/{id}";
    public static final String WORKFLOW_PENDING        = WORKFLOW_EXECUTION_BASE + "/pending";
    public static final String WORKFLOW_HISTORY        = WORKFLOW_EXECUTION_BASE + "/history/{documentId}";
    public static final String WORKFLOW_USER_INSTANCES = WORKFLOW_EXECUTION_BASE + "/user/{userId}";
    public static final String WORKFLOW_SUMMARY        = WORKFLOW_EXECUTION_BASE + "/summary";
    public static final String WORKFLOW_CANCEL         = WORKFLOW_INSTANCES + "/{id}/cancel";

    // ─── Approval endpoints (Phase 4) ─────────────────────────────────────────

    public static final String APPROVALS_BASE       = API_VERSION + "/approvals";
    public static final String APPROVAL_APPROVE     = APPROVALS_BASE + "/{id}/approve";
    public static final String APPROVAL_REJECT      = APPROVALS_BASE + "/{id}/reject";
    public static final String APPROVAL_SEND_BACK   = APPROVALS_BASE + "/{id}/send-back";
    public static final String APPROVAL_CURRENT     = APPROVALS_BASE + "/current/{instanceId}";
    public static final String APPROVAL_HISTORY     = APPROVALS_BASE + "/history/{instanceId}";
    public static final String APPROVAL_ESCALATE    = APPROVALS_BASE + "/{id}/escalate";

    // ─── Comment endpoints (Phase 4) ──────────────────────────────────────────

    public static final String COMMENTS_BASE        = API_VERSION + "/comments";
    public static final String COMMENTS_DOCUMENT    = COMMENTS_BASE + "/document/{documentId}";
    public static final String COMMENTS_THREAD      = COMMENTS_BASE + "/thread/{commentId}";
    public static final String COMMENT_BY_ID        = COMMENTS_BASE + "/{id}";

    // ─── Document endpoints (Phase 3) ────────────────────────────────────────

    public static final String DOCUMENTS_BASE              = API_VERSION + "/documents";
    public static final String DOCUMENT_BY_ID              = DOCUMENTS_BASE + "/{documentId}";
    public static final String DOCUMENT_BY_NUMBER          = DOCUMENTS_BASE + "/number/{documentNumber}";
    public static final String DOCUMENT_UPLOAD             = DOCUMENTS_BASE + "/upload";
    public static final String DOCUMENT_VERSION            = DOCUMENTS_BASE + "/{documentId}/version";
    public static final String DOCUMENT_DOWNLOAD           = DOCUMENTS_BASE + "/download/{documentId}";
    public static final String DOCUMENT_DOWNLOAD_VERSION   = DOCUMENTS_BASE + "/download/{documentId}/version/{versionNumber}";
    public static final String DOCUMENT_VERSIONS           = DOCUMENTS_BASE + "/versions/{documentId}";
    public static final String DOCUMENT_ARCHIVE            = DOCUMENTS_BASE + "/{documentId}/archive";
    public static final String DOCUMENT_RESTORE            = DOCUMENTS_BASE + "/{documentId}/restore";
    public static final String DOCUMENT_SEARCH             = DOCUMENTS_BASE + "/search";
    public static final String DOCUMENT_ADVANCED_SEARCH    = DOCUMENTS_BASE + "/advanced-search";
    public static final String DOCUMENT_SUMMARY            = DOCUMENTS_BASE + "/summary";

    // ─── Document Category endpoints (Phase 3) ───────────────────────────────

    public static final String CATEGORIES_BASE      = API_VERSION + "/categories";
    public static final String CATEGORY_BY_ID       = CATEGORIES_BASE + "/{categoryId}";
    public static final String CATEGORIES_ACTIVE    = CATEGORIES_BASE + "/active";
    public static final String CATEGORIES_PAGINATED = CATEGORIES_BASE + "/paginated";

    // ─── Notification endpoints (Phase 5) ─────────────────────────────────────

    public static final String NOTIFICATIONS_BASE = API_VERSION + "/notifications";
    public static final String NOTIFICATIONS_UNREAD = NOTIFICATIONS_BASE + "/unread";
    public static final String NOTIFICATIONS_SUMMARY = NOTIFICATIONS_BASE + "/summary";
    public static final String NOTIFICATION_READ = NOTIFICATIONS_BASE + "/{id}/read";
    public static final String NOTIFICATIONS_READ_ALL = NOTIFICATIONS_BASE + "/read-all";
    public static final String NOTIFICATION_BY_ID = NOTIFICATIONS_BASE + "/{id}";
    public static final String NOTIFICATIONS_PREFERENCES = NOTIFICATIONS_BASE + "/preferences";

    // ─── Audit endpoints (Phase 5) ────────────────────────────────────────────

    public static final String AUDIT_BASE = API_VERSION + "/audit";
    public static final String AUDIT_USER = AUDIT_BASE + "/user/{userId}";
    public static final String AUDIT_ENTITY = AUDIT_BASE + "/entity/{entityType}/{entityId}";
    public static final String AUDIT_ACTION = AUDIT_BASE + "/action/{action}";
    public static final String AUDIT_RECENT = AUDIT_BASE + "/recent";

    // ─── Dashboard endpoints (Phase 5) ────────────────────────────────────────

    public static final String DASHBOARD_BASE = API_VERSION + "/dashboard";
    public static final String DASHBOARD_STATS = DASHBOARD_BASE + "/stats";
    public static final String DASHBOARD_USER_STATS = DASHBOARD_BASE + "/user-stats";
    public static final String DASHBOARD_RECENT_ACTIVITY = DASHBOARD_BASE + "/recent-activity";
    public static final String DASHBOARD_DOCUMENT_STATS = DASHBOARD_BASE + "/document-stats";
    public static final String DASHBOARD_WORKFLOW_STATS = DASHBOARD_BASE + "/workflow-stats";

    // ─── Public (permit-all) matchers — used in SecurityConfig ───────────────

    public static final String[] PUBLIC_ENDPOINTS = {
            AUTH_LOGIN,
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };
}