// ─────────────────────────────────────────────────────────────────────────────
// useAuth.js
// Custom hook that provides convenient, type-safe access to AuthContext.
// Throws a clear error if used outside <AuthProvider>.
// ─────────────────────────────────────────────────────────────────────────────

import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

/**
 * Access the authentication context from any component inside <AuthProvider>.
 *
 * @returns {{
 *   user:            Object|null,
 *   isAuthenticated: boolean,
 *   isAdmin:         boolean,
 *   loading:         boolean,
 *   login:           (email: string, password: string) => Promise<void>,
 *   logout:          () => void,
 * }}
 *
 * @example
 * const { user, isAdmin, logout } = useAuth();
 */
const useAuth = () => {
  const context = useContext(AuthContext);

  if (context === null) {
    throw new Error(
      'useAuth() must be used inside an <AuthProvider>. ' +
      'Make sure <AuthProvider> wraps your component tree in main.jsx or AppRouter.jsx.'
    );
  }

  return context;
};

export default useAuth;