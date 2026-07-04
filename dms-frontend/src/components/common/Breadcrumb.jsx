import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';
import { ROUTES } from '../../routes/RouteConstants';

const ROUTE_LABELS = {
  [ROUTES.DASHBOARD]:   'Dashboard',
  [ROUTES.USERS]:       'Users',
  [ROUTES.DEPARTMENTS]: 'Departments',
  [ROUTES.PROFILE]:     'My Profile',
};

/**
 * Auto-generates breadcrumbs from the current URL path,
 * or renders manually supplied `items`.
 *
 * @param {Array<{label:string, to?:string}>} items  optional override
 */
const Breadcrumb = ({ items }) => {
  const location = useLocation();

  const crumbs = items ?? buildCrumbs(location.pathname);

  if (crumbs.length <= 1) return null;

  return (
    <nav aria-label="Breadcrumb" className="flex items-center gap-1 text-sm">
      <Link
        to={ROUTES.DASHBOARD}
        className="text-slate-400 hover:text-primary-600 transition-colors"
        aria-label="Dashboard"
      >
        <Home size={14} />
      </Link>

      {crumbs.map((crumb, i) => (
        <React.Fragment key={crumb.label}>
          <ChevronRight size={13} className="text-slate-300" />
          {i === crumbs.length - 1 || !crumb.to ? (
            <span className="text-slate-700 font-medium">{crumb.label}</span>
          ) : (
            <Link to={crumb.to} className="text-slate-500 hover:text-primary-600 transition-colors">
              {crumb.label}
            </Link>
          )}
        </React.Fragment>
      ))}
    </nav>
  );
};

function buildCrumbs(pathname) {
  // Match known top-level routes
  const label = ROUTE_LABELS[pathname];
  if (label) return [{ label }];
  // Unknown nested path — segment split
  const segments = pathname.split('/').filter(Boolean);
  return segments.map((seg, i) => ({
    label: seg.charAt(0).toUpperCase() + seg.slice(1).replace(/-/g, ' '),
    to:    i < segments.length - 1 ? '/' + segments.slice(0, i + 1).join('/') : undefined,
  }));
}

export default Breadcrumb;