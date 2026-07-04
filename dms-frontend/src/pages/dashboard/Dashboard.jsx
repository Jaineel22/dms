import React, { useEffect, useState } from 'react';
import { FileText, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

import PageHeader              from '../../components/common/PageHeader';
import TotalUsersWidget        from './widgets/TotalUsersWidget';
import TotalDepartmentsWidget  from './widgets/TotalDepartmentsWidget';
import ActiveUsersWidget       from './widgets/ActiveUsersWidget';
import useAuth                 from '../../hooks/useAuth';
import userApi                 from '../../api/userApi';
import departmentApi           from '../../api/departmentApi';
import { formatDateTime }      from '../../utils/formatters';

const Dashboard = () => {
  const { user } = useAuth();
  const [stats,   setStats]   = useState({ totalUsers: 0, activeUsers: 0, totalDepts: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [usersPage, deptsPage] = await Promise.all([
          userApi.getAllUsers({ page: 0, size: 1 }),
          departmentApi.getAllDepartments({ page: 0, size: 1 }),
        ]);

        // Spring Page object: { totalElements, content, ... }
        const totalUsers = usersPage?.totalElements ?? 0;
        const totalDepts = deptsPage?.totalElements ?? 0;

        // Fetch active users count (we don't have a dedicated endpoint, so we re-use search)
        // For now derive from totalElements if your backend tracks isActive
        setStats({ totalUsers, activeUsers: totalUsers, totalDepts });
      } catch {
        toast.error('Failed to load dashboard statistics.');
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  const firstName = user?.firstName || 'there';

  return (
    <div>
      <PageHeader
        title={`Welcome back, ${firstName}!`}
        subtitle={`Today is ${new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}`}
        breadcrumbs={false}
      />

      {/* ── Stat cards ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        <TotalUsersWidget       count={stats.totalUsers} loading={loading} />
        <TotalDepartmentsWidget count={stats.totalDepts} loading={loading} />
        <ActiveUsersWidget      count={stats.activeUsers} total={stats.totalUsers} loading={loading} />
      </div>

      {/* ── Activity placeholder ── */}
      <div className="card">
        <div className="card-header">
          <div className="flex items-center gap-2">
            <Clock size={17} className="text-slate-400" />
            <h3 className="font-semibold text-slate-800">Recent Activity</h3>
          </div>
        </div>
        <div className="card-body">
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <div className="w-14 h-14 bg-slate-100 rounded-full flex items-center justify-center mb-4">
              <FileText size={24} className="text-slate-400" />
            </div>
            <p className="text-slate-500 font-medium">Activity Feed</p>
            <p className="text-sm text-slate-400 mt-1">
              Audit log and activity feed will be available in Phase 5.
            </p>
          </div>
        </div>
      </div>

      {/* ── System info ── */}
      <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className="card p-4">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-3">Your Account</p>
          <dl className="space-y-2 text-sm">
            {[
              ['Name',        user?.fullName || `${user?.firstName} ${user?.lastName}`],
              ['Email',       user?.email],
              ['Employee ID', user?.employeeId],
              ['Department',  user?.department?.name],
              ['Role',        user?.role?.name?.replace('ROLE_', '')],
              ['Last Login',  formatDateTime(user?.lastLoginAt)],
            ].map(([label, value]) => (
              <div key={label} className="flex items-start gap-2">
                <dt className="w-28 text-slate-400 shrink-0">{label}</dt>
                <dd className="font-medium text-slate-700 break-all">{value || '—'}</dd>
              </div>
            ))}
          </dl>
        </div>

        <div className="card p-4">
          <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-3">System Status</p>
          <div className="space-y-2.5">
            {[
              { label: 'API Connection', status: 'Operational' },
              { label: 'Authentication',  status: 'Operational' },
              { label: 'Document Engine', status: 'Phase 3' },
              { label: 'Approval Engine', status: 'Phase 4' },
              { label: 'Notifications',   status: 'Phase 5' },
            ].map(({ label, status }) => (
              <div key={label} className="flex items-center justify-between text-sm">
                <span className="text-slate-600">{label}</span>
                <span className={[
                  'badge',
                  status === 'Operational' ? 'badge-success' : 'badge-neutral',
                ].join(' ')}>
                  {status}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;   