import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import {
  LayoutDashboard, Users, Building2, FileText,
  CheckSquare, BarChart2, Settings, HelpCircle, LogOut, X,
} from 'lucide-react';

import useAuth          from '../../hooks/useAuth';
import { ROUTES }       from '../../routes/RouteConstants';
import { getInitials, getAvatarBg } from '../../utils/helpers';
import { formatRole }   from '../../utils/formatters';
import { APP_NAME }     from '../../utils/constants';

// ─── Nav item definition ──────────────────────────────────────────────────────

const NAV_ITEMS = [
  {
    label:    'Dashboard',
    to:       ROUTES.DASHBOARD,
    icon:     LayoutDashboard,
    adminOnly: false,
  },
  {
    label:    'Users',
    to:       ROUTES.USERS,
    icon:     Users,
    adminOnly: true,
  },
  {
    label:    'Departments',
    to:       ROUTES.DEPARTMENTS,
    icon:     Building2,
    adminOnly: false,
  },
  {
    label:    'Documents',
    icon:     FileText,
    disabled: true,
    phase:    'Phase 3',
  },
  {
    label:    'Approvals',
    icon:     CheckSquare,
    disabled: true,
    phase:    'Phase 4',
  },
  {
    label:    'Reports',
    icon:     BarChart2,
    disabled: true,
    phase:    'Phase 5',
  },
];

const BOTTOM_ITEMS = [
  { label: 'Settings', icon: Settings, disabled: true },
  { label: 'Support',  icon: HelpCircle, disabled: true },
];

// ─── Component ────────────────────────────────────────────────────────────────

const Sidebar = ({ isOpen, onClose }) => {
  const { user, isAdmin, logout } = useAuth();

  const visibleNavItems = NAV_ITEMS.filter(
    (item) => !item.adminOnly || isAdmin,
  );

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-30 bg-slate-900/50 backdrop-blur-sm lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      {/* Sidebar panel */}
      <aside
        className={[
          'fixed top-0 left-0 z-40 h-screen w-64 flex flex-col',
          'bg-surface-900 text-slate-200',
          'transform transition-transform duration-300 ease-in-out',
          isOpen ? 'translate-x-0' : '-translate-x-full',
          'lg:translate-x-0 lg:static lg:z-auto',
        ].join(' ')}
        aria-label="Sidebar navigation"
      >
        {/* ── Logo ── */}
        <div className="flex items-center justify-between px-5 h-16 border-b border-white/10 shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center shrink-0">
              <FileText size={16} className="text-white" />
            </div>
            <div>
              <p className="font-bold text-white leading-none text-sm">{APP_NAME}</p>
              <p className="text-xs text-slate-400 leading-none mt-0.5">Document Management</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="lg:hidden p-1.5 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white transition-colors"
            aria-label="Close sidebar"
          >
            <X size={18} />
          </button>
        </div>

        {/* ── Main nav ── */}
        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-0.5 no-scrollbar">
          {visibleNavItems.map((item) =>
            item.disabled ? (
              <DisabledItem key={item.label} item={item} />
            ) : (
              <NavItem key={item.label} item={item} onClose={onClose} />
            ),
          )}

          <div className="my-3 border-t border-white/10" />

          {BOTTOM_ITEMS.map((item) => (
            <DisabledItem key={item.label} item={item} />
          ))}
        </nav>

        {/* ── User footer ── */}
        <div className="shrink-0 border-t border-white/10 px-3 py-3">
          <div className="flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-white/5 group">
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-semibold shrink-0 ${getAvatarBg(user?.firstName)}`}>
              {getInitials(user?.firstName, user?.lastName)}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate leading-tight">
                {user?.fullName || `${user?.firstName} ${user?.lastName}`}
              </p>
              <p className="text-xs text-slate-400 leading-tight">
                {formatRole(user?.role?.name)}
              </p>
            </div>
            <button
              onClick={logout}
              className="p-1.5 rounded-lg text-slate-500 hover:text-white hover:bg-white/10 transition-colors"
              title="Logout"
              aria-label="Logout"
            >
              <LogOut size={15} />
            </button>
          </div>
        </div>
      </aside>
    </>
  );
};

// ─── NavItem ──────────────────────────────────────────────────────────────────

const NavItem = ({ item, onClose }) => {
  const Icon = item.icon;
  return (
    <NavLink
      to={item.to}
      onClick={onClose}
      className={({ isActive }) => [
        'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
        isActive
          ? 'bg-primary-600 text-white'
          : 'text-slate-300 hover:bg-white/10 hover:text-white',
      ].join(' ')}
      end
    >
      <Icon size={17} className="shrink-0" />
      {item.label}
    </NavLink>
  );
};

const DisabledItem = ({ item }) => {
  const Icon = item.icon;
  return (
    <div className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-slate-600 cursor-not-allowed select-none">
      <Icon size={17} className="shrink-0" />
      <span>{item.label}</span>
      {item.phase && (
        <span className="ml-auto text-[10px] bg-slate-700 text-slate-400 px-1.5 py-0.5 rounded-full">
          {item.phase}
        </span>
      )}
    </div>
  );
};

export default Sidebar;