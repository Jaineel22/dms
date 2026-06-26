package com.dms.util;

import java.util.regex.Pattern;

/**
 * Stateless utility class providing field-level validation helpers.
 *
 * <p>These methods complement Jakarta Bean Validation annotations and are
 * intended for programmatic checks inside service or validator classes
 * where annotation-based validation is not available (e.g. password
 * confirmation matching, cross-field rules, manual guard clauses).</p>
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    // ─── Compiled patterns (immutable, thread-safe) ───────────────────────────

    /**
     * RFC-5322 simplified email pattern.
     * Allows: local@domain.tld, dots, hyphens, plus signs in local part.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * Password policy:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character from {@code @$!%*?&}
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    /**
     * Employee ID: uppercase letters and digits only (e.g. EMP001, ADMIN001).
     */
    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile(
            "^[A-Z0-9]+$"
    );

    /**
     * E.164 phone number (optional leading +, 7–15 digits).
     * Empty / blank string is also accepted (field is optional).
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+?[1-9]\\d{6,14})?$"
    );

    /**
     * Department code: uppercase letters, digits, underscores; 2–10 characters.
     */
    private static final Pattern DEPT_CODE_PATTERN = Pattern.compile(
            "^[A-Z0-9_]{2,10}$"
    );

    // ─── Validators ───────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the supplied string is a syntactically valid
     * email address according to the simplified RFC-5322 pattern.
     *
     * @param email value to check (null → false)
     */
    public static boolean validateEmail(String email) {
        return isNotBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Returns {@code true} if the password satisfies the project's password policy:
     * minimum 8 characters, at least one uppercase, one lowercase, one digit,
     * and one special character.
     *
     * @param password plain-text candidate (null → false)
     */
    public static boolean validatePassword(String password) {
        return isNotBlank(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Returns {@code true} if the employee ID contains only uppercase letters
     * and digits.
     *
     * @param employeeId value to check (null → false)
     */
    public static boolean validateEmployeeId(String employeeId) {
        return isNotBlank(employeeId) && EMPLOYEE_ID_PATTERN.matcher(employeeId.trim()).matches();
    }

    /**
     * Returns {@code true} if the phone number is blank/empty (optional field)
     * or matches E.164 format.
     *
     * @param phone value to check (null/blank → true, treated as not provided)
     */
    public static boolean validatePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) return true;   // optional field
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Returns {@code true} if the department code contains only uppercase
     * letters, digits, or underscores and is between 2 and 10 characters.
     *
     * @param code value to check (null → false)
     */
    public static boolean validateDepartmentCode(String code) {
        return isNotBlank(code) && DEPT_CODE_PATTERN.matcher(code.trim()).matches();
    }

    /**
     * Returns {@code true} if both password strings are non-null and equal.
     * Used for confirm-password cross-field checks.
     *
     * @param password        first password value
     * @param confirmPassword second password value
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Returns {@code true} if the string is neither null nor blank.
     *
     * @param value value to check
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Sanitises a search term for use in LIKE queries:
     * trims whitespace and strips {@code %} and {@code _} wildcard characters
     * to prevent unintended SQL wildcard injection.
     *
     * @param searchTerm raw input from the client
     * @return sanitised string, or empty string if input is null/blank
     */
    public static String sanitiseSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) return "";
        return searchTerm.trim()
                .replace("%", "")
                .replace("_", "");
    }
}