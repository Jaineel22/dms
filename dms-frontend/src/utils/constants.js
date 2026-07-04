// ─────────────────────────────────────────────────────────────────────────────
// constants.js — frontend-side constants (mirrors backend RoleConstants)
// ─────────────────────────────────────────────────────────────────────────────

// ── Roles ─────────────────────────────────────────────────────────────────────
export const ROLES = {
  ADMIN : 'ROLE_ADMIN',
  USER  : 'ROLE_USER',
};

// ── Employee levels ───────────────────────────────────────────────────────────
export const EMPLOYEE_LEVELS = [
  { value: 1, label: 'Employee'  },
  { value: 2, label: 'Team Lead' },
  { value: 3, label: 'Manager'   },
  { value: 4, label: 'Director'  },
];

// ── Pagination defaults ────────────────────────────────────────────────────────
export const PAGE_SIZES      = [10, 20, 50, 100];
export const DEFAULT_PAGE    = 0;
export const DEFAULT_SIZE    = 20;

// ── Sort options ──────────────────────────────────────────────────────────────
export const USER_SORT_OPTIONS = [
  { value: 'id,asc',         label: 'ID (Oldest first)' },
  { value: 'id,desc',        label: 'ID (Newest first)' },
  { value: 'firstName,asc',  label: 'Name A–Z' },
  { value: 'firstName,desc', label: 'Name Z–A' },
  { value: 'email,asc',      label: 'Email A–Z' },
];

export const DEPT_SORT_OPTIONS = [
  { value: 'name,asc',  label: 'Name A–Z' },
  { value: 'name,desc', label: 'Name Z–A' },
  { value: 'code,asc',  label: 'Code A–Z' },
];

// ── Toast durations (ms) ──────────────────────────────────────────────────────
export const TOAST_SUCCESS = 3000;
export const TOAST_ERROR   = 5000;

// ── Department code suggestions ───────────────────────────────────────────────
export const DEPT_CODE_SUGGESTIONS = ['HR', 'IT', 'FIN', 'LEGAL', 'OPS', 'MKTG', 'SALES', 'R&D'];

// ── App meta ──────────────────────────────────────────────────────────────────
export const APP_NAME    = import.meta.env.VITE_APP_NAME    || 'DMS';
export const APP_VERSION = import.meta.env.VITE_APP_VERSION || '1.0.0';