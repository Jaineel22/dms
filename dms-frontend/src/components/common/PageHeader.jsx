import React from 'react';
import Breadcrumb from './Breadcrumb';

/**
 * Consistent page header — title, optional subtitle, optional actions row.
 *
 * @param {string}          title
 * @param {string}          subtitle
 * @param {React.ReactNode} actions    buttons / dropdowns on the right
 * @param {Array}           breadcrumbs  passed to <Breadcrumb items=...>
 */
const PageHeader = ({ title, subtitle, actions, breadcrumbs, className = '' }) => (
  <div className={`mb-6 ${className}`}>
    {breadcrumbs !== false && (
      <div className="mb-2">
        <Breadcrumb items={breadcrumbs} />
      </div>
    )}

    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
      <div>
        <h1 className="text-xl font-bold text-slate-800 leading-tight">{title}</h1>
        {subtitle && <p className="mt-0.5 text-sm text-slate-500">{subtitle}</p>}
      </div>
      {actions && (
        <div className="flex items-center gap-2 shrink-0">{actions}</div>
      )}
    </div>
  </div>
);

export default PageHeader;