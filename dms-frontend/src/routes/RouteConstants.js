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

  // ── Dashboard ─────────────────────────────────────────────────────────────
  DASHBOARD_STATS: '/dashboard/stats',

  // ── Documents ─────────────────────────────────────────────────────────────
  DOCUMENTS:          '/documents',
  DOCUMENTS_UPLOAD:   '/documents/upload',
  DOCUMENTS_SEARCH:   '/documents/search',
  DOCUMENTS_DETAILS:  '/documents/:id',
  DOCUMENTS_EDIT:     '/documents/:id/edit',
  DOCUMENTS_VERSIONS: '/documents/:id/versions',

  // ── Approvals ─────────────────────────────────────────────────────────────
  APPROVALS_PENDING: '/approvals/pending',
  APPROVALS_HISTORY: '/approvals/history',
  APPROVALS_THREAD:  '/approvals/thread/:id',

  // ── Hierarchy ─────────────────────────────────────────────────────────────
  HIERARCHY:        '/hierarchy',
  HIERARCHY_ASSIGN: '/hierarchy/assign',
  HIERARCHY_TEAM:   '/hierarchy/team/:id',

  // ── Workflows ─────────────────────────────────────────────────────────────
  WORKFLOWS:        '/workflows',
  WORKFLOWS_CREATE: '/workflows/create',
  WORKFLOWS_EDIT:   '/workflows/:id/edit',
  WORKFLOWS_STEPS:  '/workflows/steps',
  WORKFLOWS_ASSIGN: '/workflows/assign',

  // ── Notifications ─────────────────────────────────────────────────────────
  NOTIFICATIONS:             '/notifications',
  NOTIFICATIONS_PREFERENCES: '/notifications/preferences',

  // ── Audit (Admin only) ────────────────────────────────────────────────────
  AUDIT: '/audit',

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