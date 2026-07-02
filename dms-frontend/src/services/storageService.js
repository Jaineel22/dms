// ─────────────────────────────────────────────────────────────────────────────
// storageService.js
// Centralised localStorage access for JWT token and user profile.
// All keys are read from environment variables so they never clash between
// multiple apps running on the same origin.
// ─────────────────────────────────────────────────────────────────────────────

const TOKEN_KEY = import.meta.env.VITE_TOKEN_STORAGE_KEY || 'dms_token';
const USER_KEY  = import.meta.env.VITE_USER_STORAGE_KEY  || 'dms_user';

// ─── Token ────────────────────────────────────────────────────────────────────

/**
 * Persist the JWT access token to localStorage.
 * @param {string} token
 */
const setToken = (token) => {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch (e) {
    console.error('[storageService] Failed to set token:', e);
  }
};

/**
 * Retrieve the JWT access token from localStorage.
 * @returns {string|null}
 */
const getToken = () => {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch (e) {
    console.error('[storageService] Failed to get token:', e);
    return null;
  }
};

/**
 * Remove the JWT access token from localStorage.
 */
const removeToken = () => {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch (e) {
    console.error('[storageService] Failed to remove token:', e);
  }
};

// ─── User ─────────────────────────────────────────────────────────────────────

/**
 * Persist the authenticated user object to localStorage.
 * @param {Object} user
 */
const setUser = (user) => {
  try {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  } catch (e) {
    console.error('[storageService] Failed to set user:', e);
  }
};

/**
 * Retrieve the authenticated user object from localStorage.
 * @returns {Object|null}
 */
const getUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    console.error('[storageService] Failed to get user:', e);
    return null;
  }
};

/**
 * Remove the user object from localStorage.
 */
const removeUser = () => {
  try {
    localStorage.removeItem(USER_KEY);
  } catch (e) {
    console.error('[storageService] Failed to remove user:', e);
  }
};

// ─── Combined ──────────────────────────────────────────────────────────────────

/**
 * Clear all DMS-related items from localStorage (call on logout).
 */
const clearAll = () => {
  removeToken();
  removeUser();
};

/**
 * Returns true if a token is currently persisted in localStorage.
 * @returns {boolean}
 */
const isAuthenticated = () => Boolean(getToken());

// ─── Export ───────────────────────────────────────────────────────────────────

const storageService = {
  setToken,
  getToken,
  removeToken,
  setUser,
  getUser,
  removeUser,
  clearAll,
  isAuthenticated,
};

export default storageService;