// ─────────────────────────────────────────────────────────────────────────────
// validators.js  — mirrors backend validation rules exactly
// ─────────────────────────────────────────────────────────────────────────────

// ── Patterns ──────────────────────────────────────────────────────────────────
const EMAIL_RE       = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
const PASSWORD_RE    = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
const EMPLOYEE_ID_RE = /^[A-Z0-9]+$/;
const PHONE_RE       = /^(\+?[1-9]\d{6,14})?$/;
const DEPT_CODE_RE   = /^[A-Z0-9_]{2,10}$/;

// ── Validators ────────────────────────────────────────────────────────────────

export const validateEmail = (v) => {
  if (!v?.trim()) return 'Email is required';
  if (!EMAIL_RE.test(v.trim())) return 'Enter a valid email address';
  return true;
};

export const validatePassword = (v) => {
  if (!v) return 'Password is required';
  if (!PASSWORD_RE.test(v))
    return 'Min 8 chars, with uppercase, lowercase, digit and special character (@$!%*?&)';
  return true;
};

export const validatePasswordOptional = (v) => {
  if (!v) return true;   // empty = not changing
  if (!PASSWORD_RE.test(v))
    return 'Min 8 chars, with uppercase, lowercase, digit and special character (@$!%*?&)';
  return true;
};

export const validateEmployeeId = (v) => {
  if (!v?.trim()) return 'Employee ID is required';
  if (!EMPLOYEE_ID_RE.test(v.trim())) return 'Only uppercase letters and digits allowed (e.g. EMP001)';
  return true;
};

export const validatePhoneNumber = (v) => {
  if (!v) return true;   // optional
  if (!PHONE_RE.test(v.trim())) return 'Use E.164 format (e.g. +911234567890)';
  return true;
};

export const validateDeptCode = (v) => {
  if (!v?.trim()) return 'Department code is required';
  if (!DEPT_CODE_RE.test(v.trim()))
    return 'Uppercase letters, digits and underscores only, 2–10 characters (e.g. HR, IT, FIN)';
  return true;
};

export const validateRequired = (label) => (v) =>
  v?.toString().trim() ? true : `${label} is required`;

export const validateMinLength = (min, label) => (v) =>
  !v || v.length >= min ? true : `${label} must be at least ${min} characters`;

export const validateMaxLength = (max, label) => (v) =>
  !v || v.length <= max ? true : `${label} must not exceed ${max} characters`;