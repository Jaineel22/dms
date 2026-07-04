import React from 'react';
import { Search, X } from 'lucide-react';
import Button from '../../components/ui/Button';

/**
 * Search + filter bar for the Users page.
 *
 * @param {string}   search
 * @param {Function} onSearchChange
 * @param {Function} onClear
 */
const UserFilters = ({ search = '', onSearchChange, onClear }) => (
  <div className="flex flex-col sm:flex-row gap-3">
    {/* Search */}
    <div className="relative flex-1 max-w-sm">
      <Search
        size={15}
        className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none"
      />
      <input
        type="search"
        value={search}
        onChange={(e) => onSearchChange(e.target.value)}
        placeholder="Search by name, email, employee ID…"
        className="form-input pl-9 pr-9"
      />
      {search && (
        <button
          type="button"
          onClick={onClear}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
          aria-label="Clear search"
        >
          <X size={14} />
        </button>
      )}
    </div>
  </div>
);

export default UserFilters;