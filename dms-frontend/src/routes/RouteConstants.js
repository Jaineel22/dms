// ─────────────────────────────────────────────────────────────────────────────
// RouteConstants.js
// Single source of truth for every client-side route path.
// Import ROUTES wherever you need to navigate or render <Route path=...>.
// ─────────────────────────────────────────────────────────────────────────────

export const ROUTES = {
  // ── Public ────────────────────────────────────────────────────────────────
  LOGIN: '/login',

  // ── Root redirect ──────────────────────────────────────────────────────────
  ROOT: '/',

  // ── Protected ─────────────────────────────────────────────────────────────
  DASHBOARD:   '/dashboard',
  PROFILE:     '/profile',

  // ── Admin-only ────────────────────────────────────────────────────────────
  USERS:          '/users',
  USERS_EDIT:     '/users/:id/edit',   // helper — use buildUserEditPath()

  // ── Admin + User ──────────────────────────────────────────────────────────
  DEPARTMENTS:     '/departments',
  DEPARTMENTS_EDIT:'/departments/:id/edit',

  // ── Fallback ──────────────────────────────────────────────────────────────
  NOT_FOUND: '*',
};

// ─── Path builders ────────────────────────────────────────────────────────────
// Use these instead of manually interpolating IDs into path strings.

/**
 * @param {number|string} id
 * @returns {string} e.g. '/users/42/edit'
 */
export const buildUserEditPath = (id) => `/users/${id}/edit`;

/**
 * @param {number|string} id
 * @returns {string} e.g. '/departments/3/edit'
 */
export const buildDeptEditPath = (id) => `/departments/${id}/edit`;