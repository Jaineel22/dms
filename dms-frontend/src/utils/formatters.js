// ─────────────────────────────────────────────────────────────────────────────
// formatters.js
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Format an ISO date string or Date object into a readable date.
 * @param {string|Date|number[]|null} value  Backend may send LocalDateTime as int array
 * @param {Intl.DateTimeFormatOptions} [opts]
 */
export const formatDate = (value, opts = {}) => {
  if (!value) return '—';

  let date;
  if (Array.isArray(value)) {
    // Spring Boot LocalDateTime serialised as [year,month,day,hour,min,sec,nano]
    const [y, mo, d, h = 0, mi = 0, s = 0] = value;
    date = new Date(y, mo - 1, d, h, mi, s);
  } else {
    date = new Date(value);
  }

  if (isNaN(date.getTime())) return '—';

  const defaults = { day: '2-digit', month: 'short', year: 'numeric', ...opts };
  return date.toLocaleDateString('en-IN', defaults);
};

/**
 * Format a date with time.
 */
export const formatDateTime = (value) =>
  formatDate(value, {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });

/**
 * Relative time (e.g. "2 hours ago").
 */
export const formatRelativeTime = (value) => {
  if (!value) return '—';
  const date = Array.isArray(value)
    ? new Date(value[0], value[1] - 1, value[2], value[3] ?? 0, value[4] ?? 0)
    : new Date(value);

  if (isNaN(date.getTime())) return '—';

  const diffMs   = Date.now() - date.getTime();
  const diffMins = Math.floor(diffMs / 60_000);

  if (diffMins < 1)   return 'Just now';
  if (diffMins < 60)  return `${diffMins} min ago`;
  const diffHrs = Math.floor(diffMins / 60);
  if (diffHrs  < 24)  return `${diffHrs} hr ago`;
  const diffDays = Math.floor(diffHrs / 24);
  if (diffDays < 7)   return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  return formatDate(value);
};

/**
 * Format role name for display.
 * 'ROLE_ADMIN' → 'Admin', 'ROLE_USER' → 'User'
 */
export const formatRole = (roleName) => {
  if (!roleName) return '—';
  return roleName
    .replace(/^ROLE_/, '')
    .charAt(0).toUpperCase() +
    roleName.replace(/^ROLE_/, '').slice(1).toLowerCase();
};

/**
 * Format employee level integer to label.
 */
export const formatEmployeeLevel = (level) => {
  const map = { 1: 'Employee', 2: 'Team Lead', 3: 'Manager', 4: 'Director' };
  return map[level] ?? '—';
};

/**
 * Truncate a string to maxLen characters, appending '…'.
 */
export const truncate = (str, maxLen = 40) => {
  if (!str) return '—';
  return str.length > maxLen ? str.slice(0, maxLen) + '…' : str;
};

/**
 * Format a number with thousands separators.
 */
export const formatNumber = (n) => {
  if (n == null) return '0';
  return Number(n).toLocaleString('en-IN');
};

/**
 * Format file size bytes to human-readable string.
 */
export const formatFileSize = (bytes) => {
  if (!bytes) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  let v = bytes;
  while (v >= 1024 && i < units.length - 1) { v /= 1024; i++; }
  return `${v.toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
};