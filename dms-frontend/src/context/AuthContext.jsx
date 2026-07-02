// ─────────────────────────────────────────────────────────────────────────────
// AuthContext.jsx
// Provides authentication state and actions to the entire component tree.
//
// Exposed via context:
//   user          - UserResponse object | null
//   isAuthenticated - boolean
//   isAdmin       - boolean (true when user.role.name === 'ROLE_ADMIN')
//   loading       - boolean (true during initial session restore + login)
//   login(email, password) → Promise<void>
//   logout()               → void
// ─────────────────────────────────────────────────────────────────────────────

import React, {
  createContext,
  useState,
  useEffect,
  useCallback,
  useMemo,
} from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

import authApi        from '../api/authApi';
import storageService from '../services/storageService';
import { ROUTES }     from '../routes/RouteConstants';

// ─── Context creation ─────────────────────────────────────────────────────────

export const AuthContext = createContext(null);

// ─── Provider ─────────────────────────────────────────────────────────────────

/**
 * Wrap your component tree with <AuthProvider> (done in main.jsx / AppRouter).
 * Must be rendered inside <BrowserRouter> so useNavigate() works.
 */
export const AuthProvider = ({ children }) => {
  const navigate = useNavigate();

  // ── State ──────────────────────────────────────────────────────────────────
  const [user,    setUser]    = useState(null);
  const [loading, setLoading] = useState(true); // true during initial hydration

  // ── Derived booleans ───────────────────────────────────────────────────────
  const isAuthenticated = Boolean(user);
  const isAdmin = user?.role?.name === 'ROLE_ADMIN';

  // ── Session restore — runs once on mount ───────────────────────────────────
  useEffect(() => {
    const restoreSession = async () => {
      const token       = storageService.getToken();
      const storedUser  = storageService.getUser();

      if (!token || !storedUser) {
        // No stored session — stay unauthenticated
        setLoading(false);
        return;
      }

      // We have a cached token — rehydrate from storage immediately so the UI
      // doesn't flash the login page, then validate against the server.
      setUser(storedUser);

      try {
        // Validate the token and get a fresh user profile
        const freshUser = await authApi.getCurrentUser();
        setUser(freshUser);
        storageService.setUser(freshUser);
      } catch {
        // Token is expired / invalid — clear everything
        _clearSession();
      } finally {
        setLoading(false);
      }
    };

    restoreSession();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ── Login ──────────────────────────────────────────────────────────────────

  /**
   * Authenticate with email + password.
   * Stores token and user, then navigates to /dashboard.
   *
   * @param {string} email
   * @param {string} password
   * @throws {Object} Normalised error from axiosInstance interceptor
   */
  const login = useCallback(async (email, password) => {
    setLoading(true);
    try {
      // authApi.login returns the inner `data` from the ApiResponse envelope:
      // { token, tokenType, expiresIn, user }
      const { token, user: userResponse } = await authApi.login(email, password);

      storageService.setToken(token);
      storageService.setUser(userResponse);
      setUser(userResponse);

      toast.success(`Welcome back, ${userResponse.firstName}!`);
      navigate(ROUTES.DASHBOARD, { replace: true });
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  // ── Logout ─────────────────────────────────────────────────────────────────

  /**
   * Clear all auth state and navigate to /login.
   * Fires the server-side logout endpoint best-effort (non-blocking).
   */
  const logout = useCallback(() => {
    // Best-effort server-side invalidation — don't await, don't block UX
    authApi.logout().catch(() => {});

    _clearSession();
    toast.success('You have been logged out.');
    navigate(ROUTES.LOGIN, { replace: true });
  }, [navigate]);

  // ── Internal helpers ───────────────────────────────────────────────────────

  const _clearSession = () => {
    storageService.clearAll();
    setUser(null);
  };

  // ── Context value (memoised to prevent unnecessary re-renders) ─────────────
  const value = useMemo(() => ({
    user,
    isAuthenticated,
    isAdmin,
    loading,
    login,
    logout,
  }), [user, isAuthenticated, isAdmin, loading, login, logout]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthContext;