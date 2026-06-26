package com.dms.validator;

import com.dms.dto.request.UserCreateRequest;
import com.dms.dto.request.UserUpdateRequest;
import com.dms.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring-managed validator for {@link UserCreateRequest} and
 * {@link UserUpdateRequest}. Performs cross-field and business-rule
 * validations that cannot be expressed with Jakarta Bean Validation
 * annotations alone.
 *
 * <p>Usage in service layer:</p>
 * <pre>
 *   List&lt;String&gt; errors = userValidator.validateCreate(request);
 *   if (!errors.isEmpty()) throw new ValidationException(errors);
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class UserValidator {

    // ─── Create ───────────────────────────────────────────────────────────────

    /**
     * Validates a {@link UserCreateRequest} beyond Jakarta annotations.
     * Returns a list of human-readable error messages; empty list = valid.
     *
     * @param request the incoming create payload
     * @return list of validation errors (empty if all pass)
     */
    public List<String> validateCreate(UserCreateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Request body must not be null");
            return errors;
        }

        // Email format (double-check beyond @Email — avoids edge cases)
        if (!ValidationUtils.validateEmail(request.getEmail())) {
            errors.add("Email address format is invalid");
        }

        // Password policy
        if (!ValidationUtils.validatePassword(request.getPassword())) {
            errors.add("Password must be at least 8 characters and contain at least one "
                    + "uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)");
        }

        // Employee ID format
        if (!ValidationUtils.validateEmployeeId(request.getEmployeeId())) {
            errors.add("Employee ID must contain only uppercase letters and digits (e.g. EMP001)");
        }

        // Phone number (optional — only validate if present)
        if (!ValidationUtils.validatePhoneNumber(request.getPhoneNumber())) {
            errors.add("Phone number must be in E.164 format (e.g. +911234567890)");
        }

        // Employee level range
        if (request.getEmployeeLevel() != null
                && (request.getEmployeeLevel() < 1 || request.getEmployeeLevel() > 4)) {
            errors.add("Employee level must be between 1 (Employee) and 4 (Director)");
        }

        // Self-manager guard
        // Note: userId is not available at creation time — this guard exists at service level.
        // Included here for completeness of documentation.

        return errors;
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    /**
     * Validates a {@link UserUpdateRequest} beyond Jakarta annotations.
     * Only validates fields that are non-null (patch semantics).
     *
     * @param request the incoming update payload
     * @return list of validation errors (empty if all pass)
     */
    public List<String> validateUpdate(UserUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Request body must not be null");
            return errors;
        }

        // Email — only validate if supplied
        if (ValidationUtils.isNotBlank(request.getEmail())
                && !ValidationUtils.validateEmail(request.getEmail())) {
            errors.add("Email address format is invalid");
        }

        // Phone — only validate if supplied
        if (ValidationUtils.isNotBlank(request.getPhoneNumber())
                && !ValidationUtils.validatePhoneNumber(request.getPhoneNumber())) {
            errors.add("Phone number must be in E.164 format (e.g. +911234567890)");
        }

        // Employee level range — only validate if supplied
        if (request.getEmployeeLevel() != null
                && (request.getEmployeeLevel() < 1 || request.getEmployeeLevel() > 4)) {
            errors.add("Employee level must be between 1 (Employee) and 4 (Director)");
        }

        return errors;
    }

    // ─── Password ─────────────────────────────────────────────────────────────

    /**
     * Validates that a new password satisfies the project's password policy.
     *
     * @param newPassword candidate plain-text password
     * @return {@code true} if valid
     */
    public boolean isValidPassword(String newPassword) {
        return ValidationUtils.validatePassword(newPassword);
    }
}