import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Activity } from 'lucide-react';
import dashboardApi from '../../../api/dashboardApi';

/**
 * Accepts an optional `activities` prop (e.g. from DashboardStats' single
 * getDashboardStats() call, which already embeds recentActivities) so the
 * widget doesn't have to re-fetch when it's already been loaded by a parent.
 * Falls back to fetching its own data via dashboardApi.getRecentActivity()
 * when used standalone.
 */
export default function RecentActivityWidget({ activities: providedActivities, limit = 10 }) {
  const navigate = useNavigate();
  const [activities, setActivities] = useState(providedActivities || null);
  const [loading, setLoading] = useState(!providedActivities);

  useEffect(() => {
    if (providedActivities) {
      setActivities(providedActivities);
      return;
    }
    (async () => {
      setLoading(true);
      try {
        const data = await dashboardApi.getRecentActivity(limit);
        setActivities(data || []);
      } catch {
        toast.error('Failed to load recent activity');
      } finally {
        setLoading(false);
      }
    })();
  }, [providedActivities, limit]);

  const handleClick = (activity) => {
    if (activity.entityType === 'DOCUMENT') navigate(`/documents/${activity.entityId}`);
    else if (activity.entityType === 'WORKFLOW_INSTANCE') navigate(`/approvals/history/${activity.entityId}`);
  };

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-5">
      <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-900">
        <Activity className="h-4 w-4" /> Recent Activity
      </h2>
      {loading && <p className="text-sm text-gray-400">Loading...</p>}
      {!loading && (!activities || activities.length === 0) && (
        <p className="text-sm text-gray-400">No recent activity</p>
      )}
      {!loading && activities && activities.length > 0 && (
        <ul className="divide-y divide-gray-100">
          {activities.map((a) => (
            <li key={a.id}>
              <button
                onClick={() => handleClick(a)}
                className="flex w-full items-center justify-between py-2.5 text-left text-sm hover:bg-gray-50"
              >
                <span>
                  <span className="font-medium text-gray-900">{a.userFullName || 'Unknown User'}</span>{' '}
                  <span className="text-gray-500">{a.action?.toLowerCase()}</span>{' '}
                  <span className="text-gray-700">{a.entityName || `${a.entityType} #${a.entityId}`}</span>
                </span>
                <span className="whitespace-nowrap text-xs text-gray-400">
                  {a.createdAt ? new Date(a.createdAt).toLocaleString() : ''}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}