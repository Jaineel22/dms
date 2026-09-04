import { useEffect, useState } from 'react';
import { Clock, FileText, ClipboardCheck } from 'lucide-react';
import toast from 'react-hot-toast';

import PageHeader        from '../../components/common/PageHeader';
import TotalUsersWidget  from './widgets/TotalUsersWidget';
import ActiveUsersWidget from './widgets/ActiveUsersWidget';
import useAuth            from '../../hooks/useAuth';
import dashboardApi       from '../../api/dashboardApi';
import { formatDateTime, formatNumber } from '../../utils/formatters';

/** Small stat card matching the styling of the widgets/*Widget.jsx components. */
const StatCard = ({ icon: Icon, iconBg, iconColor, label, value, caption, loading }) => (
  <div className="stat-card">
    <div className={`stat-icon ${iconBg}`}>
      <Icon size={22} className={iconColor} />
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-xs font-medium text-slate-500 uppercase tracking-wide">{label}</p>
      {loading ? (
        <div className="h-7 w-16 skeleton rounded mt-1" />
      ) : (
        <p className="text-2xl font-bold text-slate-800 mt-0.5">{formatNumber(value)}</p>
      )}
      {caption && <p className="text-xs text-slate-400 mt-1">{caption}</p>}
    </div>
  </div>
);

const DOC_STATUS_LABELS = [
  ['draftCount',       'Draft'],
  ['underReviewCount', 'Under Review'],
  ['approvedCount',    'Approved'],
  ['rejectedCount',    'Rejected'],
  ['archivedCount',    'Archived'],
];

const Dashboard = () => {
  const { user, isAdmin } = useAuth();
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    const fetchStats = async () => {
      setLoading(true);
      try {
        const data = isAdmin
          ? await dashboardApi.getDashboardStats()
          : await dashboardApi.getUserDashboardStats();
        if (!cancelled) setStats(data);
      } catch {
        if (!cancelled) toast.error('Failed to load dashboard statistics.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchStats();
    return () => { cancelled = true; };
  }, [isAdmin]);

  const firstName = user?.firstName || 'there';
  const activities = stats?.recentActivities || [];

  return (
    <div>
      <PageHeader
        title={`Welcome back, ${firstName}!`}
        subtitle={`Today is ${new Date().toLocaleDateString('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}`}
        breadcrumbs={false}
      />

      {/* ── Stat cards ── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {isAdmin && (
          <>
            <TotalUsersWidget
              count={stats?.totalUsers ?? 0}
              loading={loading}
            />
            <ActiveUsersWidget
              count={stats?.activeUsers ?? 0}
              total={stats?.totalUsers ?? 0}
              loading={loading}
            />
          </>
        )}
        <StatCard
          icon={FileText}
          iconBg="bg-amber-100"
          iconColor="text-amber-600"
          label={isAdmin ? 'Total Documents' : 'My Documents'}
          value={stats?.totalDocuments ?? 0}
          caption={isAdmin ? 'Across the system' : 'Documents you own'}
          loading={loading}
        />
        <StatCard
          icon={ClipboardCheck}
          iconBg="bg-rose-100"
          iconColor="text-rose-600"
          label="Pending Approvals"
          value={stats?.pendingApprovals ?? 0}
          caption={isAdmin ? 'Awaiting action, system-wide' : 'Your submissions awaiting action'}
          loading={loading}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* ── Recent activity ── */}
        <div className="card">
          <div className="card-header">
            <div className="flex items-center gap-2">
              <Clock size={17} className="text-slate-400" />
              <h3 className="font-semibold text-slate-800">Recent Activity</h3>
            </div>
          </div>
          <div className="card-body">
            {loading ? (
              <div className="space-y-3 py-2">
                {[...Array(4)].map((_, i) => <div key={i} className="h-9 skeleton rounded" />)}
              </div>
            ) : activities.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <div className="w-14 h-14 bg-slate-100 rounded-full flex items-center justify-center mb-4">
                  <FileText size={24} className="text-slate-400" />
                </div>
                <p className="text-slate-500 font-medium">No recent activity</p>
                <p className="text-sm text-slate-400 mt-1">
                  Actions you take (or that happen system-wide) will show up here.
                </p>
              </div>
            ) : (
              <ul className="divide-y divide-slate-100">
                {activities.map((a) => (
                  <li key={a.id} className="py-2.5 text-sm">
                    <p className="text-slate-700">
                      <span className="font-medium">{a.userFullName || 'Someone'}</span>{' '}
                      <span className="text-slate-500">{(a.action || '').toLowerCase().replaceAll('_', ' ')}</span>{' '}
                      {a.entityType && (
                        <span className="text-slate-500">
                          {a.entityType.toLowerCase()}{a.entityName ? ` "${a.entityName}"` : ''}
                        </span>
                      )}
                    </p>
                    <p className="text-xs text-slate-400 mt-0.5">{formatDateTime(a.createdAt)}</p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* ── Document breakdown ── */}
        <div className="card">
          <div className="card-header">
            <div className="flex items-center gap-2">
              <FileText size={17} className="text-slate-400" />
              <h3 className="font-semibold text-slate-800">Document Breakdown</h3>
            </div>
          </div>
          <div className="card-body">
            <div className="space-y-2.5">
              {DOC_STATUS_LABELS.map(([key, label]) => (
                <div key={key} className="flex items-center justify-between text-sm">
                  <span className="text-slate-600">{label}</span>
                  <span className="badge badge-neutral">
                    {loading ? '—' : formatNumber(stats?.[key] ?? 0)}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* ── Account info ── */}
      <div className="mt-4 card p-4">
        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-3">Your Account</p>
        <dl className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm">
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
    </div>
  );
};

export default Dashboard;
