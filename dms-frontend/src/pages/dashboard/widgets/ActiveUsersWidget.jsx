import React from 'react';
import { UserCheck } from 'lucide-react';
import { formatNumber } from '../../../utils/formatters';

const ActiveUsersWidget = ({ count = 0, total = 0, loading = false }) => {
  const pct = total > 0 ? Math.round((count / total) * 100) : 0;

  return (
    <div className="stat-card">
      <div className="stat-icon bg-purple-100">
        <UserCheck size={22} className="text-purple-600" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-xs font-medium text-slate-500 uppercase tracking-wide">Active Users</p>
        {loading ? (
          <div className="h-7 w-20 skeleton rounded mt-1" />
        ) : (
          <p className="text-2xl font-bold text-slate-800 mt-0.5">{formatNumber(count)}</p>
        )}
        <p className="text-xs text-slate-400 mt-1">
          {loading ? '—' : `${pct}% of total`}
        </p>
      </div>
    </div>
  );
};

export default ActiveUsersWidget;