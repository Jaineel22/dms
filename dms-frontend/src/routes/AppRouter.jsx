import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

import { AuthProvider }   from '../context/AuthContext';
import PrivateRoute       from './PrivateRoute';
import { ROUTES }         from './RouteConstants';
import LoadingSpinner     from '../components/common/LoadingSpinner';

// ─── Lazy page imports ────────────────────────────────────────────────────────
const Login           = lazy(() => import('../pages/auth/Login.jsx'));
const Dashboard       = lazy(() => import('../pages/dashboard/Dashboard.jsx'));
const DashboardStats  = lazy(() => import('../pages/dashboard/DashboardStats.jsx'));
const UserList        = lazy(() => import('../pages/users/UserList.jsx'));
const DepartmentList  = lazy(() => import('../pages/departments/DepartmentList.jsx'));
const Profile         = lazy(() => import('../pages/profile/Profile.jsx'));
const DashboardLayout = lazy(() => import('../components/layout/DashboardLayout.jsx'));

// Documents
const DocumentList           = lazy(() => import('../pages/documents/DocumentList.jsx'));
const DocumentUpload         = lazy(() => import('../pages/documents/DocumentUpload.jsx'));
const DocumentSearch         = lazy(() => import('../pages/documents/DocumentSearch.jsx'));
const DocumentDetails        = lazy(() => import('../pages/documents/DocumentDetails.jsx'));
const DocumentEdit           = lazy(() => import('../pages/documents/DocumentEdit.jsx'));
const DocumentVersionHistory = lazy(() => import('../pages/documents/DocumentVersionHistory.jsx'));

// Approvals
const PendingApprovals = lazy(() => import('../pages/approvals/PendingApprovals.jsx'));
const ApprovalHistory  = lazy(() => import('../pages/approvals/ApprovalHistory.jsx'));
const ApprovalThread   = lazy(() => import('../pages/approvals/ApprovalThread.jsx'));

// Hierarchy
const HierarchyTree     = lazy(() => import('../pages/hierarchy/HierarchyTree.jsx'));
const ManagerAssignment = lazy(() => import('../pages/hierarchy/ManagerAssignment.jsx'));
const TeamView          = lazy(() => import('../pages/hierarchy/TeamView.jsx'));

// Workflows
const WorkflowList           = lazy(() => import('../pages/workflows/WorkflowList.jsx'));
const WorkflowForm           = lazy(() => import('../pages/workflows/WorkflowForm.jsx'));
const WorkflowStepForm       = lazy(() => import('../pages/workflows/WorkflowStepForm.jsx'));
const UserWorkflowAssignment = lazy(() => import('../pages/workflows/UserWorkflowAssignment.jsx'));

// Notifications
const NotificationList        = lazy(() => import('../pages/notifications/NotificationList.jsx'));
const NotificationPreferences = lazy(() => import('../pages/notifications/NotificationPreferences.jsx'));

// Audit
const AuditLogList = lazy(() => import('../pages/audit/AuditLogList.jsx'));

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
            <Route path={ROUTES.DASHBOARD}       element={<Dashboard />} />
            <Route path={ROUTES.DASHBOARD_STATS} element={<DashboardStats />} />
            <Route path={ROUTES.DEPARTMENTS}     element={<DepartmentList />} />
            <Route path={ROUTES.PROFILE}         element={<Profile />} />

            {/* Documents */}
            <Route path={ROUTES.DOCUMENTS}          element={<DocumentList />} />
            <Route path={ROUTES.DOCUMENTS_UPLOAD}   element={<DocumentUpload />} />
            <Route path={ROUTES.DOCUMENTS_SEARCH}   element={<DocumentSearch />} />
            <Route path={ROUTES.DOCUMENTS_DETAILS}  element={<DocumentDetails />} />
            <Route path={ROUTES.DOCUMENTS_EDIT}     element={<DocumentEdit />} />
            <Route path={ROUTES.DOCUMENTS_VERSIONS} element={<DocumentVersionHistory />} />

            {/* Approvals */}
            <Route path={ROUTES.APPROVALS_PENDING} element={<PendingApprovals />} />
            <Route path={ROUTES.APPROVALS_HISTORY} element={<ApprovalHistory />} />
            <Route path={ROUTES.APPROVALS_THREAD}  element={<ApprovalThread />} />

            {/* Hierarchy */}
            <Route path={ROUTES.HIERARCHY}        element={<HierarchyTree />} />
            <Route path={ROUTES.HIERARCHY_ASSIGN} element={<ManagerAssignment />} />
            <Route path={ROUTES.HIERARCHY_TEAM}   element={<TeamView />} />

            {/* Workflows */}
            <Route path={ROUTES.WORKFLOWS}        element={<WorkflowList />} />
            <Route path={ROUTES.WORKFLOWS_CREATE} element={<WorkflowForm />} />
            <Route path={ROUTES.WORKFLOWS_EDIT}   element={<WorkflowForm />} />
            <Route path={ROUTES.WORKFLOWS_STEPS}  element={<WorkflowStepForm />} />
            <Route path={ROUTES.WORKFLOWS_ASSIGN} element={<UserWorkflowAssignment />} />

            {/* Notifications */}
            <Route path={ROUTES.NOTIFICATIONS}             element={<NotificationList />} />
            <Route path={ROUTES.NOTIFICATIONS_PREFERENCES} element={<NotificationPreferences />} />
          </Route>
        </Route>

        {/* Protected — ADMIN only */}
        <Route element={<PrivateRoute adminOnly />}>
          <Route element={<DashboardLayout />}>
            <Route path={ROUTES.USERS} element={<UserList />} />
            <Route path={ROUTES.AUDIT} element={<AuditLogList />} />
          </Route>
        </Route>

        {/* 404 */}
        <Route path={ROUTES.NOT_FOUND} element={<NotFound />} />

      </Routes>
    </Suspense>
  </AuthProvider>
);

export default AppRouter;