// ─────────────────────────────────────────────────────────────────────────────
// authApi.js
// Authentication API functions: login, logout, getCurrentUser.
// All functions return Promises and resolve with the `data` field of the
// ApiResponse envelope returned by the Spring Boot backend.
// ─────────────────────────────────────────────────────────────────────────────

import axiosInstance from './axios';

// ─── Endpoints ────────────────────────────────────────────────────────────────

const AUTH_ENDPOINTS = {
  LOGIN:   '/auth/login',
  LOGOUT:  '/auth/logout',
  ME:      '/auth/me',
};

// ─── Functions ────────────────────────────────────────────────────────────────

/**
 * Authenticate with email + password.
 * On success the backend returns:
 *   { status: true, data: { token, tokenType, expiresIn, user } }
 *
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{ token: string, tokenType: string, expiresIn: number, user: Object }>}
 */
const login = async (email, password) => {
  const response = await axiosInstance.post(AUTH_ENDPOINTS.LOGIN, {
    email,
    password,
  });
  // Unwrap the ApiResponse envelope → return the inner data object
  return response.data.data;
};

/**
 * Invalidate the current session on the server side.
 * (Stateless JWT — server logs the event; client discards token regardless.)
 *
 * @returns {Promise<void>}
 */
const logout = async () => {
  try {
    await axiosInstance.post(AUTH_ENDPOINTS.LOGOUT);
  } catch {
    // Swallow errors — client-side cleanup proceeds regardless
  }
};

/**
 * Fetch the profile of the currently authenticated user.
 *
 * @returns {Promise<Object>} UserResponse
 */
const getCurrentUser = async () => {
  const response = await axiosInstance.get(AUTH_ENDPOINTS.ME);
  return response.data.data;
};

// ─── Export ───────────────────────────────────────────────────────────────────

const authApi = {
  login,
  logout,
  getCurrentUser,
};

export default authApi;