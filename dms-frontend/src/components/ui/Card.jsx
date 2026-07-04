import React from 'react';

/**
 * Card shell with optional header, body and footer slots.
 *
 * Usage:
 *   <Card>
 *     <Card.Header title="Users" action={<Button>Add</Button>} />
 *     <Card.Body>...</Card.Body>
 *     <Card.Footer>...</Card.Footer>
 *   </Card>
 */
const Card = ({ children, className = '', noPadding = false }) => (
  <div className={`bg-white rounded-xl border border-slate-200 shadow-card overflow-hidden ${className}`}>
    {children}
  </div>
);

Card.Header = ({ title, subtitle, action, className = '', children }) => (
  <div className={`px-6 py-4 border-b border-slate-200 flex items-center justify-between gap-4 ${className}`}>
    <div className="min-w-0">
      {title && <h3 className="font-semibold text-slate-800 text-base leading-tight">{title}</h3>}
      {subtitle && <p className="text-sm text-slate-500 mt-0.5">{subtitle}</p>}
      {children}
    </div>
    {action && <div className="shrink-0">{action}</div>}
  </div>
);

Card.Body = ({ children, className = '' }) => (
  <div className={`px-6 py-5 ${className}`}>{children}</div>
);

Card.Footer = ({ children, className = '' }) => (
  <div className={`px-6 py-4 bg-slate-50 border-t border-slate-200 ${className}`}>{children}</div>
);

export default Card;