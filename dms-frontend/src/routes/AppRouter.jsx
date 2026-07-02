import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

import { AuthProvider }   from '../context/AuthContext';
import PrivateRoute       from './PrivateRoute';
import { ROUTES }         from './RouteConstants';
import LoadingSpinner     from '../components/common/LoadingSpinner';

// ─── Lazy page imports ────────────────────────────────────────────────────────
const Login           = lazy(() => import('../pages/auth/Login.jsx'));
const Dashboard       = lazy(() => import('../pages/dashboard/Dashboard.jsx'));
const UserList        = lazy(() => import('../pages/users/UserList.jsx'));
const DepartmentList  = lazy(() => import('../pages/departments/DepartmentList.jsx'));
const Profile         = lazy(() => import('../pages/profile/Profile.jsx'));
const DashboardLayout = lazy(() => import('../components/layout/DashboardLayout.jsx'));

// ─── 404 ─────────────────────────────────────────────────────────────────────
const NotFound = () => (
  <div className="min-h-screen flex items-center justify-center bg-surface-50">
    <div className="text-center px-4">
      <h1 className="text-8xl font-bold text-primary-600">404</h1>
      <p className="mt-4 text-2xl font-semibold text-slate-700">Page not found</p>
      <p className="mt-2 text-slate-500 text-sm">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <a
        href={ROUTES.DASHBOARD}
        className="mt-6 inline-flex items-center px-4 py-2 bg-primary-600 text-white
                   rounded-lg text-sm font-medium hover:bg-primary-700 transition-colors"
      >
        Back to Dashboard
      </a>
    </div>
  </div>
);

// ─── Full-page loading fallback ───────────────────────────────────────────────
const PageSpinner = () => (
  <div className="min-h-screen flex items-center justify-center bg-surface-50">
    <LoadingSpinner size="lg" />
  </div>
);

// ─── Router ───────────────────────────────────────────────────────────────────
const AppRouter = () => (
  <AuthProvider>
    <Suspense fallback={<PageSpinner />}>
      <Routes>

        {/* Root redirect */}
        <Route path={ROUTES.ROOT} element={<Navigate to={ROUTES.DASHBOARD} replace />} />

        {/* Public */}
        <Route path={ROUTES.LOGIN} element={<Login />} />

        {/* Protected — any authenticated user */}
        <Route element={<PrivateRoute />}>
          <Route element={<DashboardLayout />}>
            <Route path={ROUTES.DASHBOARD}   element={<Dashboard />} />
            <Route path={ROUTES.DEPARTMENTS} element={<DepartmentList />} />
            <Route path={ROUTES.PROFILE}     element={<Profile />} />
          </Route>
        </Route>

        {/* Protected — ADMIN only */}
        <Route element={<PrivateRoute adminOnly />}>
          <Route element={<DashboardLayout />}>
            <Route path={ROUTES.USERS} element={<UserList />} />
          </Route>
        </Route>

        {/* 404 */}
        <Route path={ROUTES.NOT_FOUND} element={<NotFound />} />

      </Routes>
    </Suspense>
  </AuthProvider>
);

export default AppRouter;