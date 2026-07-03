package com.dms.constant;

/**
 * Centralised error message strings used by {@code GlobalExceptionHandler}
 * and service layer exception throws.
 */
public final class ErrorConstants {

    private ErrorConstants() {}

    // ─── Generic ──────────────────────────────────────────────────────────────

    public static final String INTERNAL_ERROR       = "An unexpected error occurred. Please try again later.";
    public static final String VALIDATION_FAILED    = "Request validation failed. Please check the submitted data.";
    public static final String MALFORMED_REQUEST    = "Malformed JSON request body.";

    // ─── Authentication ───────────────────────────────────────────────────────

    public static final String INVALID_CREDENTIALS      = "Invalid email or password.";
    public static final String ACCOUNT_DISABLED         = "Account is disabled. Please contact an administrator.";
    public static final String ACCOUNT_LOCKED           = "Account is temporarily locked due to too many failed login attempts.";
    public static final String AUTHENTICATION_REQUIRED  = "Authentication is required to access this resource.";
    public static final String TOKEN_EXPIRED            = "Authentication token has expired. Please log in again.";
    public static final String TOKEN_INVALID            = "Authentication token is invalid.";

    // ─── Authorization ────────────────────────────────────────────────────────

    public static final String ACCESS_DENIED            = "You do not have permission to perform this action.";
    public static final String ADMIN_ONLY               = "This action requires administrator privileges.";

    // ─── User ─────────────────────────────────────────────────────────────────

    public static final String USER_NOT_FOUND           = "User not found.";
    public static final String USER_EMAIL_EXISTS        = "A user with this email address already exists.";
    public static final String USER_EMPLOYEE_ID_EXISTS  = "A user with this employee ID already exists.";
    public static final String USER_SELF_MANAGER        = "A user cannot be assigned as their own manager.";
    public static final String USER_PASSWORD_INCORRECT  = "Current password is incorrect.";

    // ─── Department ───────────────────────────────────────────────────────────

    public static final String DEPT_NOT_FOUND           = "Department not found.";
    public static final String DEPT_NAME_EXISTS         = "A department with this name already exists.";
    public static final String DEPT_CODE_EXISTS         = "A department with this code already exists.";
    public static final String DEPT_HAS_USERS           = "Cannot delete department — it still has active users assigned.";

    // ─── Role ─────────────────────────────────────────────────────────────────

    public static final String ROLE_NOT_FOUND           = "Role not found.";

    // ─── Hierarchy ────────────────────────────────────────────────────────────

    public static final String HIERARCHY_USER_NOT_FOUND        = "User not found for hierarchy operation.";
    public static final String HIERARCHY_MANAGER_NOT_FOUND     = "Manager not found.";
    public static final String HIERARCHY_CIRCULAR_REFERENCE    = "Circular reference detected in hierarchy. User cannot be their own manager.";
    public static final String HIERARCHY_SELF_MANAGER          = "A user cannot be their own manager.";
    public static final String HIERARCHY_EMPLOYEE_LEVEL_INVALID = "Manager must have a higher employee level than the user.";
    public static final String HIERARCHY_ALREADY_HAS_MANAGER   = "This user already has a manager assigned.";
    public static final String HIERARCHY_NO_MANAGER_ASSIGNED   = "This user does not have a manager assigned.";

    // ─── Workflow ─────────────────────────────────────────────────────────────

    public static final String WORKFLOW_NOT_FOUND        = "Workflow not found.";
    public static final String WORKFLOW_NAME_EXISTS      = "A workflow with this name already exists.";
    public static final String WORKFLOW_STEPS_INVALID    = "Workflow steps are invalid.";
    public static final String WORKFLOW_STEP_NOT_FOUND   = "Workflow step not found.";
    public static final String WORKFLOW_INVALID_STEP_ORDER = "Workflow steps must be sequential (1, 2, 3, ...).";
    public static final String WORKFLOW_DUPLICATE_STEP   = "Duplicate step numbers are not allowed.";
    public static final String WORKFLOW_STEP_LEVEL_INVALID = "Approval level must be between 1 and 4.";
    public static final String USER_WORKFLOW_EXISTS      = "This workflow is already assigned to the user.";
    public static final String USER_WORKFLOW_NOT_FOUND   = "Workflow is not assigned to this user.";
    public static final String WORKFLOW_MIN_STEPS        = "Workflow must have at least 2 steps.";
    public static final String WORKFLOW_ROLE_NOT_FOUND   = "Role name does not exist in the system.";
}