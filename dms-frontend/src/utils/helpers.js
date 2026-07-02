// ─────────────────────────────────────────────────────────────────────────────
// helpers.js
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Debounce — returns a function that delays invoking fn until after `wait` ms
 * have elapsed since the last call.
 *
 * @param {Function} fn
 * @param {number}   wait  ms
 */
export const debounce = (fn, wait = 300) => {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => fn(...args), wait);
  };
};

/**
 * Get initials from a full name or first+last name pair.
 * 'John Doe' → 'JD'
 *
 * @param {string} firstName
 * @param {string} [lastName]
 */
export const getInitials = (firstName, lastName) => {
  if (!firstName) return '?';
  const f = firstName.charAt(0).toUpperCase();
  const l = lastName ? lastName.charAt(0).toUpperCase() : '';
  return f + l;
};

/**
 * Map a user's isActive status to a Tailwind badge class.
 *
 * @param {boolean} isActive
 * @returns {{ bg: string, text: string, dot: string, label: string }}
 */
export const getStatusColor = (isActive) =>
  isActive
    ? { bg: 'bg-green-100',  text: 'text-green-800',  dot: 'bg-green-500',  label: 'Active'   }
    : { bg: 'bg-red-100',    text: 'text-red-800',    dot: 'bg-red-500',    label: 'Inactive' };

/**
 * Map a role name to a Tailwind badge class.
 *
 * @param {string} roleName  e.g. 'ROLE_ADMIN'
 */
export const getRoleColor = (roleName) => {
  switch (roleName) {
    case 'ROLE_ADMIN': return { bg: 'bg-purple-100', text: 'text-purple-800' };
    default:           return { bg: 'bg-blue-100',   text: 'text-blue-800'   };
  }
};

/**
 * Map an employee level integer to a display colour.
 *
 * @param {number} level  1–4
 */
export const getLevelColor = (level) => {
  const map = {
    1: { bg: 'bg-gray-100',   text: 'text-gray-700'   },
    2: { bg: 'bg-cyan-100',   text: 'text-cyan-700'   },
    3: { bg: 'bg-orange-100', text: 'text-orange-700' },
    4: { bg: 'bg-rose-100',   text: 'text-rose-700'   },
  };
  return map[level] ?? map[1];
};

/**
 * Build a consistent avatar background colour from a name string.
 * Returns a Tailwind bg class.
 *
 * @param {string} name
 */
export const getAvatarBg = (name = '') => {
  const colours = [
    'bg-blue-500', 'bg-indigo-500', 'bg-violet-500', 'bg-purple-500',
    'bg-pink-500',  'bg-rose-500',   'bg-orange-500', 'bg-teal-500',
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return colours[Math.abs(hash) % colours.length];
};

/**
 * Produce a query string from an object, omitting undefined / empty-string values.
 *
 * @param {Object} params
 * @returns {string}  e.g. '?page=0&size=20'
 */
export const buildQueryString = (params) => {
  const qs = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== '' && v !== null)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&');
  return qs ? `?${qs}` : '';
};

/**
 * Sleep for `ms` milliseconds — useful in tests / demos.
 */
export const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/**
 * Safely parse JSON, returning `fallback` on any error.
 */
export const safeJsonParse = (str, fallback = null) => {
  try { return JSON.parse(str); } catch { return fallback; }
};