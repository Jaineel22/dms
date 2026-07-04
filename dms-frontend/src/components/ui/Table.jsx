import React from 'react';
import { ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';

/**
 * Responsive table with striped rows, sort headers, empty state and loading skeleton.
 *
 * @param {Array}  columns   [{ key, label, sortable?, render?, className?, width? }]
 * @param {Array}  data      row objects
 * @param {string} sortKey   current sort field
 * @param {string} sortDir   'asc' | 'desc'
 * @param {Function} onSort  (key) => void
 * @param {boolean} loading
 * @param {string}  emptyText
 * @param {string}  rowKey   field to use as React key (default 'id')
 */
const Table = ({
  columns   = [],
  data      = [],
  sortKey,
  sortDir   = 'asc',
  onSort,
  loading   = false,
  emptyText = 'No records found.',
  rowKey    = 'id',
  className = '',
}) => {
  const handleSort = (col) => {
    if (!col.sortable || !onSort) return;
    onSort(col.key);
  };

  const SortIcon = ({ colKey }) => {
    if (colKey !== sortKey) return <ChevronsUpDown size={13} className="text-slate-400" />;
    return sortDir === 'asc'
      ? <ChevronUp   size={13} className="text-primary-600" />
      : <ChevronDown size={13} className="text-primary-600" />;
  };

  return (
    <div className={`overflow-x-auto ${className}`}>
      <table className="w-full border-collapse text-sm">
        {/* ── Head ── */}
        <thead>
          <tr className="bg-slate-50 border-b border-slate-200">
            {columns.map((col) => (
              <th
                key={col.key}
                style={col.width ? { width: col.width } : undefined}
                className={[
                  'px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide',
                  'whitespace-nowrap select-none',
                  col.sortable ? 'cursor-pointer hover:bg-slate-100 transition-colors' : '',
                  col.className ?? '',
                ].join(' ')}
                onClick={() => handleSort(col)}
              >
                <span className="flex items-center gap-1">
                  {col.label}
                  {col.sortable && <SortIcon colKey={col.key} />}
                </span>
              </th>
            ))}
          </tr>
        </thead>

        {/* ── Body ── */}
        <tbody>
          {loading ? (
            // Skeleton rows
            Array.from({ length: 5 }).map((_, i) => (
              <tr key={i} className="border-b border-slate-100">
                {columns.map((col) => (
                  <td key={col.key} className="px-4 py-3">
                    <div className="h-4 skeleton rounded" style={{ width: col.skeletonWidth ?? '80%' }} />
                  </td>
                ))}
              </tr>
            ))
          ) : data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="px-4 py-12 text-center text-slate-400">
                <div className="flex flex-col items-center gap-2">
                  <svg className="w-10 h-10 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
                      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  <span className="text-sm">{emptyText}</span>
                </div>
              </td>
            </tr>
          ) : (
            data.map((row, idx) => (
              <tr
                key={row[rowKey] ?? idx}
                className={[
                  'border-b border-slate-100 transition-colors',
                  idx % 2 === 0 ? 'bg-white' : 'bg-slate-50/50',
                  'hover:bg-primary-50/40',
                ].join(' ')}
              >
                {columns.map((col) => (
                  <td key={col.key} className={`px-4 py-3 text-slate-700 ${col.className ?? ''}`}>
                    {col.render ? col.render(row[col.key], row) : (row[col.key] ?? '—')}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default Table;