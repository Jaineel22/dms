import React from 'react';
import { Users, TrendingUp } from 'lucide-react';
import { formatNumber } from '../../../utils/formatters';

const TotalUsersWidget = ({ count = 0, loading = false }) => (
  <div className="stat-card">
    <div className="stat-icon bg-blue-100">
      <Users size={22} className="text-blue-600" />
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-xs font-medium text-slate-500 uppercase tracking-wide">Total Users</p>
      {loading ? (
        <div className="h-7 w-20 skeleton rounded mt-1" />
      ) : (
        <p className="text-2xl font-bold text-slate-800 mt-0.5">{formatNumber(count)}</p>
      )}
      <p className="flex items-center gap-1 text-xs text-green-600 mt-1">
        <TrendingUp size={12} />
        <span>All registered users</span>
      </p>
    </div>
  </div>
);

export default TotalUsersWidget;