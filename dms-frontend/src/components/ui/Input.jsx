import React, { forwardRef, useState } from 'react';
import { Eye, EyeOff, AlertCircle } from 'lucide-react';

/**
 * Form input with label, optional icon, error display, and password toggle.
 */
const Input = forwardRef(({
  label,
  error,
  hint,
  type       = 'text',
  leftIcon,
  rightIcon,
  required   = false,
  disabled   = false,
  className  = '',
  inputClassName = '',
  id,
  ...props
}, ref) => {
  const [showPwd, setShowPwd] = useState(false);
  const inputId  = id || props.name;
  const isPassword = type === 'password';
  const resolvedType = isPassword ? (showPwd ? 'text' : 'password') : type;

  const baseInput = [
    'block w-full rounded-lg border text-sm text-slate-800 placeholder-slate-400',
    'transition-all duration-150',
    'focus:outline-none focus:ring-2',
    disabled ? 'bg-slate-50 text-slate-400 cursor-not-allowed' : 'bg-white',
    error
      ? 'border-red-400 focus:border-red-500 focus:ring-red-500/20'
      : 'border-slate-300 focus:border-primary-500 focus:ring-primary-500/20',
    leftIcon  ? 'pl-10'  : 'pl-3',
    (isPassword || rightIcon) ? 'pr-10' : 'pr-3',
    'py-2',
    inputClassName,
  ].join(' ');

  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      {label && (
        <label htmlFor={inputId} className="text-sm font-medium text-slate-700 flex items-center gap-1">
          {label}
          {required && <span className="text-red-500">*</span>}
        </label>
      )}

      <div className="relative">
        {leftIcon && (
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">
            {leftIcon}
          </span>
        )}

        <input
          ref={ref}
          id={inputId}
          type={resolvedType}
          disabled={disabled}
          className={baseInput}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${inputId}-error` : undefined}
          {...props}
        />

        {isPassword && (
          <button
            type="button"
            tabIndex={-1}
            onClick={() => setShowPwd((p) => !p)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
          >
            {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        )}

        {rightIcon && !isPassword && (
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">
            {rightIcon}
          </span>
        )}
      </div>

      {error && (
        <p id={`${inputId}-error`} className="flex items-center gap-1 text-xs text-red-600 mt-0.5" role="alert">
          <AlertCircle size={12} />
          {error}
        </p>
      )}

      {hint && !error && (
        <p className="text-xs text-slate-500 mt-0.5">{hint}</p>
      )}
    </div>
  );
});

Input.displayName = 'Input';
export default Input;