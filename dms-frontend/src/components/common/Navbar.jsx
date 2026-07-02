import React, { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Menu, Bell, ChevronDown, User, LogOut, Settings } from 'lucide-react';

import useAuth          from '../../hooks/useAuth';
import Breadcrumb       from './Breadcrumb';
import { ROUTES }       from '../../routes/RouteConstants';
import { getInitials, getAvatarBg } from '../../utils/helpers';
import { formatRole }   from '../../utils/formatters';

const Navbar = ({ onMenuToggle }) => {
  const { user, isAdmin, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const fullName = user?.fullName || `${user?.firstName ?? ''} ${user?.lastName ?? ''}`.trim();

  return (
    <header className="h-16 bg-white border-b border-slate-200 flex items-center px-4 gap-3 shrink-0 shadow-navbar z-20">
      {/* Hamburger — mobile only */}
      <button
        onClick={onMenuToggle}
        className="p-2 rounded-lg text-slate-500 hover:bg-slate-100 hover:text-slate-700 lg:hidden transition-colors"
        aria-label="Toggle sidebar"
      >
        <Menu size={20} />
      </button>

      {/* Breadcrumb */}
      <div className="flex-1 min-w-0 hidden sm:block">
        <Breadcrumb />
      </div>

      <div className="flex items-center gap-2 ml-auto">
        {/* Notifications — Phase 5 placeholder */}
        <button
          className="relative p-2 rounded-lg text-slate-500 hover:bg-slate-100 transition-colors"
          aria-label="Notifications (coming soon)"
          title="Notifications (Phase 5)"
          disabled
        >
          <Bell size={18} />
        </button>

        {/* Profile dropdown */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setDropdownOpen((o) => !o)}
            className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-slate-100 transition-colors"
            aria-haspopup="true"
            aria-expanded={dropdownOpen}
            aria-label="User menu"
          >
            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-semibold shrink-0 ${getAvatarBg(user?.firstName)}`}>
              {getInitials(user?.firstName, user?.lastName)}
            </div>
            <div className="hidden md:block text-left">
              <p className="text-sm font-medium text-slate-700 leading-tight truncate max-w-[120px]">
                {fullName}
              </p>
              <p className="text-xs text-slate-400 leading-tight">
                {formatRole(user?.role?.name)}
              </p>
            </div>
            <ChevronDown
              size={15}
              className={`text-slate-400 transition-transform duration-200 ${dropdownOpen ? 'rotate-180' : ''}`}
            />
          </button>

          {/* Dropdown menu */}
          {dropdownOpen && (
            <div className="absolute right-0 mt-2 w-52 bg-white rounded-xl shadow-modal border border-slate-200 py-1.5 z-50 animate-slide-in">
              {/* User info header */}
              <div className="px-3 py-2.5 border-b border-slate-100">
                <p className="text-sm font-semibold text-slate-800 truncate">{fullName}</p>
                <p className="text-xs text-slate-500 truncate">{user?.email}</p>
                {isAdmin && (
                  <span className="mt-1 inline-flex items-center px-1.5 py-0.5 text-[10px] font-medium bg-purple-100 text-purple-700 rounded">
                    Administrator
                  </span>
                )}
              </div>

              <Link
                to={ROUTES.PROFILE}
                onClick={() => setDropdownOpen(false)}
                className="flex items-center gap-2.5 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50 transition-colors"
              >
                <User size={15} className="text-slate-400" />
                My Profile
              </Link>

              <button
                disabled
                className="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-slate-400 cursor-not-allowed"
              >
                <Settings size={15} />
                Settings <span className="ml-auto text-[10px] text-slate-300">Phase 5</span>
              </button>

              <div className="border-t border-slate-100 mt-1 pt-1">
                <button
                  onClick={() => { setDropdownOpen(false); logout(); }}
                  className="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                >
                  <LogOut size={15} />
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;