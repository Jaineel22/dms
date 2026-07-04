import React from 'react';
import { Building2 } from 'lucide-react';
import { formatNumber } from '../../../utils/formatters';

const TotalDepartmentsWidget = ({ count = 0, loading = false }) => (
  <div className="stat-card">
    <div className="stat-icon bg-green-100">
      <Building2 size={22} className="text-green-600" />
    </div>
    <div className="flex-1 min-w-0">
      <p className="text-xs font-medium text-slate-500 uppercase tracking-wide">Departments</p>
      {loading ? (
        <div className="h-7 w-16 skeleton rounded mt-1" />
      ) : (
        <p className="text-2xl font-bold text-slate-800 mt-0.5">{formatNumber(count)}</p>
      )}
      <p className="text-xs text-slate-400 mt-1">Active departments</p>
    </div>
  </div>
);

export default TotalDepartmentsWidget;