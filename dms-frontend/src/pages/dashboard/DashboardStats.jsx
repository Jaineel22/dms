import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Users, FileText, GitBranch, HardDrive } from 'lucide-react';
import dashboardApi from '../../api/dashboardApi';
import RecentActivityWidget from './widgets/RecentActivityWidget';
import PendingApprovalsWidget from './widgets/PendingApprovalsWidget';

function StatCard({ icon: Icon, label, value, tone = 'blue' }) {
  const tones = {
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    yellow: 'bg-yellow-50 text-yellow-600',
    red: 'bg-red-50 text-red-600',
    gray: 'bg-gray-50 text-gray-600',
  };
  return (
    <div className="flex items-center gap-3 rounded-lg border border-gray-200 bg-white p-4">
      <span className={`flex h-10 w-10 items-center justify-center rounded-full ${tones[tone]}`}>
        <Icon className="h-5 w-5" />
      </span>
      <div>
        <p className="text-xs text-gray-500">{label}</p>
        <p className="text-lg font-semibold text-gray-900">{value ?? '-'}</p>
      </div>
    </div>
  );
}

export default function DashboardStats() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const data = await dashboardApi.getDashboardStats();
        setStats(data);
      } catch {
        toast.error('Failed to load dashboard statistics');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="space-y-8">
      <h1 className="text-xl font-semibold text-gray-900">Dashboard</h1>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-gray-500">Users</h2>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
          <StatCard icon={Users} label="Total Users" value={stats?.totalUsers} tone="blue" />
          <StatCard icon={Users} label="Active Users" value={stats?.activeUsers} tone="green" />
          <StatCard icon={Users} label="Inactive Users" value={stats?.inactiveUsers} tone="gray" />
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-gray-500">Documents</h2>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
          <StatCard icon={FileText} label="Total" value={stats?.totalDocuments} tone="blue" />
          <StatCard icon={FileText} label="Draft" value={stats?.draftCount} tone="gray" />
          <StatCard icon={FileText} label="Under Review" value={stats?.underReviewCount} tone="yellow" />
          <StatCard icon={FileText} label="Approved" value={stats?.approvedCount} tone="green" />
          <StatCard icon={FileText} label="Rejected" value={stats?.rejectedCount} tone="red" />
          <StatCard icon={FileText} label="Archived" value={stats?.archivedCount} tone="gray" />
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-gray-500">Workflows</h2>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-6">
          <StatCard icon={GitBranch} label="Total" value={stats?.totalWorkflows} tone="blue" />
          <StatCard icon={GitBranch} label="Pending" value={stats?.pendingApprovals} tone="yellow" />
          <StatCard icon={GitBranch} label="In Progress" value={stats?.inProgressWorkflows} tone="yellow" />
          <StatCard icon={GitBranch} label="Approved" value={stats?.approvedWorkflows} tone="green" />
          <StatCard icon={GitBranch} label="Rejected" value={stats?.rejectedWorkflows} tone="red" />
          <StatCard icon={GitBranch} label="Expired" value={stats?.expiredWorkflows} tone="gray" />
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-gray-500">Storage</h2>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3">
          <StatCard icon={HardDrive} label="Total Storage Used" value={stats?.totalStorageReadable} tone="blue" />
        </div>
      </section>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <RecentActivityWidget activities={stats?.recentActivities} />
        <PendingApprovalsWidget />
      </div>
    </div>
  );
}