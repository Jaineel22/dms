import React from 'react';
import { CheckCircle2, XCircle, AlertTriangle, Info, X } from 'lucide-react';

const VARIANTS = {
  success: {
    wrapper: 'bg-green-50 border-green-200 text-green-800',
    icon:    <CheckCircle2  size={18} className="text-green-600 shrink-0 mt-0.5" />,
  },
  error: {
    wrapper: 'bg-red-50 border-red-200 text-red-800',
    icon:    <XCircle       size={18} className="text-red-600 shrink-0 mt-0.5" />,
  },
  warning: {
    wrapper: 'bg-yellow-50 border-yellow-200 text-yellow-800',
    icon:    <AlertTriangle size={18} className="text-yellow-600 shrink-0 mt-0.5" />,
  },
  info: {
    wrapper: 'bg-blue-50 border-blue-200 text-blue-800',
    icon:    <Info          size={18} className="text-blue-600 shrink-0 mt-0.5" />,
  },
};

/**
 * Inline alert banner with optional dismiss.
 *
 * @param {'success'|'error'|'warning'|'info'} variant
 * @param {string}   title       optional bold heading
 * @param {boolean}  dismissible show ✕ button
 * @param {Function} onDismiss   called when ✕ is clicked
 */
const Alert = ({
  variant     = 'info',
  title,
  children,
  dismissible = false,
  onDismiss,
  className   = '',
}) => {
  const { wrapper, icon } = VARIANTS[variant] ?? VARIANTS.info;

  return (
    <div
      role="alert"
      className={`flex gap-3 rounded-lg border p-4 text-sm ${wrapper} ${className}`}
    >
      {icon}
      <div className="flex-1 min-w-0">
        {title && <p className="font-semibold mb-0.5">{title}</p>}
        {children && <div className="text-sm leading-relaxed">{children}</div>}
      </div>
      {dismissible && onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          className="shrink-0 p-0.5 rounded hover:opacity-70 transition-opacity"
          aria-label="Dismiss"
        >
          <X size={15} />
        </button>
      )}
    </div>
  );
};

export default Alert;