import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';

import Sidebar from '../common/Sidebar';
import Navbar  from '../common/Navbar';

/**
 * Main application shell: fixed sidebar (collapsible on mobile) + sticky navbar + scrollable content.
 */
const DashboardLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen overflow-hidden bg-surface-50">
      {/* Sidebar */}
      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      {/* Main area */}
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        {/* Navbar */}
        <Navbar onMenuToggle={() => setSidebarOpen((o) => !o)} />

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 page-enter">
          <Outlet />
        </main>

        {/* Footer */}
        <footer className="shrink-0 px-6 py-3 border-t border-slate-200 bg-white">
          <p className="text-xs text-slate-400 text-center">
            © {new Date().getFullYear()} DMS — Document Management System &nbsp;·&nbsp; Version 1.0.0
          </p>
        </footer>
      </div>
    </div>
  );
};

export default DashboardLayout;