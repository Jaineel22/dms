import React from 'react';

/**
 * @param {'sm'|'md'|'lg'|'xl'} size
 * @param {boolean} fullPage  centres vertically in the viewport
 * @param {string}  label     accessible label (screen reader only)
 */
const LoadingSpinner = ({ size = 'md', fullPage = false, label = 'Loading…', className = '' }) => {
  const sizeMap = { sm: 'w-4 h-4 border-2', md: 'w-8 h-8 border-3', lg: 'w-12 h-12 border-4', xl: 'w-16 h-16 border-4' };
  const ring = `rounded-full border-primary-200 border-t-primary-600 animate-spin ${sizeMap[size] ?? sizeMap.md}`;

  const spinner = (
    <div role="status" className={`flex flex-col items-center gap-3 ${className}`}>
      <div className={ring} />
      <span className="sr-only">{label}</span>
    </div>
  );

  if (fullPage) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface-50">
        {spinner}
      </div>
    );
  }

  return spinner;
};

export default LoadingSpinner;