import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

/**
 * Pagination control.
 *
 * @param {number}   page          current zero-based page
 * @param {number}   totalPages
 * @param {number}   totalElements
 * @param {number}   size          page size
 * @param {Function} onPageChange  (newPage: number) => void
 */
const Pagination = ({
  page          = 0,
  totalPages    = 1,
  totalElements = 0,
  size          = 20,
  onPageChange,
  className     = '',
}) => {
  if (totalPages <= 1) return null;

  const from  = page * size + 1;
  const to    = Math.min((page + 1) * size, totalElements);
  const pages = buildPageList(page, totalPages);

  const go = (p) => {
    if (p >= 0 && p < totalPages && p !== page) onPageChange?.(p);
  };

  return (
    <div className={`flex flex-col sm:flex-row items-center justify-between gap-3 px-4 py-3 ${className}`}>
      {/* Info text */}
      <p className="text-xs text-slate-500 whitespace-nowrap">
        Showing <span className="font-medium text-slate-700">{from}–{to}</span> of{' '}
        <span className="font-medium text-slate-700">{totalElements}</span> results
      </p>

      {/* Controls */}
      <nav className="flex items-center gap-1" aria-label="Pagination">
        {/* Prev */}
        <button
          onClick={() => go(page - 1)}
          disabled={page === 0}
          className="p-1.5 rounded-lg border border-slate-300 text-slate-600
                     hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          aria-label="Previous page"
        >
          <ChevronLeft size={15} />
        </button>

        {/* Page numbers */}
        {pages.map((p, i) =>
          p === '...' ? (
            <span key={`dot-${i}`} className="w-8 text-center text-xs text-slate-400 select-none">…</span>
          ) : (
            <button
              key={p}
              onClick={() => go(p)}
              className={[
                'w-8 h-8 text-xs rounded-lg border transition-colors',
                p === page
                  ? 'bg-primary-600 border-primary-600 text-white font-semibold'
                  : 'border-slate-300 text-slate-600 hover:bg-slate-50',
              ].join(' ')}
              aria-current={p === page ? 'page' : undefined}
            >
              {p + 1}
            </button>
          )
        )}

        {/* Next */}
        <button
          onClick={() => go(page + 1)}
          disabled={page >= totalPages - 1}
          className="p-1.5 rounded-lg border border-slate-300 text-slate-600
                     hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          aria-label="Next page"
        >
          <ChevronRight size={15} />
        </button>
      </nav>
    </div>
  );
};

/** Build a compact page list with ellipsis for large page counts. */
function buildPageList(current, total) {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i);
  const pages = new Set([0, total - 1, current]);
  for (let i = Math.max(0, current - 1); i <= Math.min(total - 1, current + 1); i++) pages.add(i);
  const sorted = [...pages].sort((a, b) => a - b);
  const result = [];
  let prev = -1;
  for (const p of sorted) {
    if (p - prev > 1) result.push('...');
    result.push(p);
    prev = p;
  }
  return result;
}

export default Pagination;