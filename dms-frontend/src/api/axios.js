// ─────────────────────────────────────────────────────────────────────────────
// axios.js
// Configured Axios instance shared by all API modules.
//
// Request interceptor  → attaches Bearer token from localStorage.
// Response interceptor → on 401, clears storage and redirects to /login.
// ─────────────────────────────────────────────────────────────────────────────

import axios from 'axios';
import storageService from '../services/storageService';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// ─── Instance ─────────────────────────────────────────────────────────────────

const axiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30_000,
  headers: {
    'Content-Type': 'application/json',
    'Accept':       'application/json',
  },
});

// ─── Request interceptor — attach JWT ─────────────────────────────────────────

axiosInstance.interceptors.request.use(
  (config) => {
    const token = storageService.getToken();
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// ─── Response interceptor — handle 401 ───────────────────────────────────────

axiosInstance.interceptors.response.use(
  // Pass-through for successful responses
  (response) => response,

  // Error handler
  (error) => {
    if (error.response?.status === 401) {
      // Clear all auth data and force a hard redirect to login.
      // Using window.location instead of React Router navigate() because this
      // interceptor lives outside the React component tree.
      storageService.clearAll();

      // Avoid redirect loop if already on /login
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    // Normalise the error so callers always get { message, status, data }
    const normalisedError = {
      message:
        error.response?.data?.message ||
        error.message ||
        'An unexpected error occurred',
      status:  error.response?.status || 0,
      data:    error.response?.data   || null,
      errors:  error.response?.data?.errors || [],
    };

    return Promise.reject(normalisedError);
  },
);

export default axiosInstance;