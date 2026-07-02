// ─────────────────────────────────────────────────────────────────────────────
// PrivateRoute.jsx
// Route guard component used in AppRouter.
//
// Behaviours:
//   • While auth is loading → show full-screen spinner (no flash of login page)
//   • Not authenticated     → redirect to /login (preserves intended destination)
//   • adminOnly + not admin → redirect to /dashboard with an informational toast
//   • Authenticated (+ role OK) → render <Outlet /> (nested child routes)
// ─────────────────────────────────────────────────────────────────────────────

import React, { useEffect } from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';

import useAuth     from '../hooks/useAuth';
import { ROUTES }  from './RouteConstants';

// ─── Full-screen spinner (duplicated here to keep PrivateRoute self-contained) ─

const FullPageSpinner = () => (
  <div className="min-h-screen flex items-center justify-center bg-surface-50">
    <div className="flex flex-col items-center gap-3">
      <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
      <p className="text-sm text-slate-500 font-medium">Loading…</p>
    </div>
  </div>
);

// ─── Component ────────────────────────────────────────────────────────────────

/**
 * Wrap private <Route> groups with this component.
 *
 * @param {Object}  props
 * @param {boolean} [props.adminOnly=false] - When true, only ROLE_ADMIN users pass through
 *
 * @example
 * // Any authenticated user
 * <Route element={<PrivateRoute />}>
 *   <Route path="/dashboard" element={<Dashboard />} />
 * </Route>
 *
 * // ADMIN only
 * <Route element={<PrivateRoute adminOnly />}>
 *   <Route path="/users" element={<UserList />} />
 * </Route>
 */
const PrivateRoute = ({ adminOnly = false }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  const location = useLocation();

  // Show a toast when an unauthorised admin-only access is attempted.
  // useEffect keeps the toast out of the render cycle.
  useEffect(() => {
    if (!loading && isAuthenticated && adminOnly && !isAdmin) {
      toast.error('You do not have permission to access that page.');
    }
  }, [loading, isAuthenticated, adminOnly, isAdmin]);

  // 1. Still restoring session from localStorage — don't flash the login page
  if (loading) {
    return <FullPageSpinner />;
  }

  // 2. Not logged in → send to login, preserving the intended destination
  if (!isAuthenticated) {
    return (
      <Navigate
        to={ROUTES.LOGIN}
        state={{ from: location }}
        replace
      />
    );
  }

  // 3. Logged in but lacks the required role → send to dashboard
  if (adminOnly && !isAdmin) {
    return <Navigate to={ROUTES.DASHBOARD} replace />;
  }

  // 4. All checks passed — render children
  return <Outlet />;
};

export default PrivateRoute;