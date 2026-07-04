import React from 'react';
import { FileText } from 'lucide-react';
import { APP_NAME, APP_VERSION } from '../../utils/constants';

/**
 * Simple centred layout for the Login page.
 */
const AuthLayout = ({ children }) => (
  <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-slate-900 via-surface-800 to-primary-950 px-4 py-12">

    {/* Logo mark */}
    <div className="mb-8 flex flex-col items-center gap-3">
      <div className="w-14 h-14 bg-primary-600 rounded-2xl flex items-center justify-center shadow-lg">
        <FileText size={28} className="text-white" />
      </div>
      <div className="text-center">
        <h1 className="text-2xl font-bold text-white tracking-tight">{APP_NAME}</h1>
        <p className="text-sm text-slate-400 mt-0.5">Enterprise Document Management System</p>
      </div>
    </div>

    {/* Card */}
    <div className="w-full max-w-sm bg-white rounded-2xl shadow-modal p-8">
      {children}
    </div>

    {/* Footer */}
    <p className="mt-6 text-xs text-slate-500 text-center">
      Internal Use Only &nbsp;·&nbsp; v{APP_VERSION}
    </p>
  </div>
);

export default AuthLayout;