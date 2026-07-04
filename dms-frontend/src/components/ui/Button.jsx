import React from 'react';
import { Loader2 } from 'lucide-react';

const VARIANTS = {
  primary:   'bg-primary-600 text-white hover:bg-primary-700 active:bg-primary-800 focus-visible:ring-primary-500',
  secondary: 'bg-white text-slate-700 border border-slate-300 hover:bg-slate-50 active:bg-slate-100 focus-visible:ring-slate-400',
  danger:    'bg-red-600 text-white hover:bg-red-700 active:bg-red-800 focus-visible:ring-red-500',
  outline:   'bg-transparent text-primary-600 border border-primary-600 hover:bg-primary-50 focus-visible:ring-primary-500',
  ghost:     'bg-transparent text-slate-600 hover:bg-slate-100 focus-visible:ring-slate-400',
  success:   'bg-green-600 text-white hover:bg-green-700 focus-visible:ring-green-500',
};

const SIZES = {
  xs: 'px-2.5 py-1.5 text-xs gap-1',
  sm: 'px-3 py-1.5 text-sm gap-1.5',
  md: 'px-4 py-2 text-sm gap-2',
  lg: 'px-5 py-2.5 text-base gap-2',
};

/**
 * Versatile button component.
 *
 * @param {'primary'|'secondary'|'danger'|'outline'|'ghost'|'success'} variant
 * @param {'xs'|'sm'|'md'|'lg'} size
 * @param {boolean} loading   - shows spinner and disables
 * @param {boolean} fullWidth - w-full
 * @param {React.ReactNode} leftIcon
 * @param {React.ReactNode} rightIcon
 */
const Button = ({
  children,
  variant = 'primary',
  size    = 'md',
  loading = false,
  disabled = false,
  fullWidth = false,
  leftIcon,
  rightIcon,
  className = '',
  type = 'button',
  ...props
}) => {
  const base = [
    'inline-flex items-center justify-center font-medium rounded-lg',
    'transition-all duration-150',
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
    'disabled:opacity-50 disabled:cursor-not-allowed',
    VARIANTS[variant] || VARIANTS.primary,
    SIZES[size]       || SIZES.md,
    fullWidth ? 'w-full' : '',
    className,
  ].join(' ');

  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={base}
      {...props}
    >
      {loading ? (
        <Loader2 size={size === 'xs' || size === 'sm' ? 14 : 16} className="animate-spin shrink-0" />
      ) : leftIcon ? (
        <span className="shrink-0">{leftIcon}</span>
      ) : null}

      {children && <span>{children}</span>}

      {!loading && rightIcon && (
        <span className="shrink-0">{rightIcon}</span>
      )}
    </button>
  );
};

export default Button;