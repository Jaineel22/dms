// ─────────────────────────────────────────────────────────────────────────────
// userApi.js
// User management API functions matching all 8 UserController endpoints.
// ─────────────────────────────────────────────────────────────────────────────

import axiosInstance from './axios';

// ─── Endpoints ────────────────────────────────────────────────────────────────

const USERS_BASE = '/users';

// ─── Functions ────────────────────────────────────────────────────────────────

/**
 * Fetch paginated list of users. Optionally filter by search keyword.
 *
 * @param {Object} params
 * @param {number} [params.page=0]       - Zero-based page number
 * @param {number} [params.size=20]      - Page size
 * @param {string} [params.sort='id,asc'] - Sort field and direction
 * @param {string} [params.search='']   - Search keyword
 * @returns {Promise<Page<UserResponse>>}
 */
const getAllUsers = async ({
  page   = 0,
  size   = 20,
  sort   = 'id,asc',
  search = '',
} = {}) => {
  const params = { page, size, sort };
  if (search?.trim()) params.search = search.trim();

  const response = await axiosInstance.get(USERS_BASE, { params });
  return response.data.data;
};

/**
 * Fetch a single user by ID.
 *
 * @param {number} id
 * @returns {Promise<UserResponse>}
 */
const getUserById = async (id) => {
  const response = await axiosInstance.get(`${USERS_BASE}/${id}`);
  return response.data.data;
};

/**
 * Create a new user (ADMIN only).
 *
 * @param {Object} data - UserCreateRequest payload
 * @returns {Promise<UserResponse>}
 */
const createUser = async (data) => {
  const response = await axiosInstance.post(USERS_BASE, data);
  return response.data.data;
};

/**
 * Update an existing user (ADMIN only).
 *
 * @param {number} id
 * @param {Object} data - UserUpdateRequest payload (patch semantics — only send changed fields)
 * @returns {Promise<UserResponse>}
 */
const updateUser = async (id, data) => {
  const response = await axiosInstance.put(`${USERS_BASE}/${id}`, data);
  return response.data.data;
};

/**
 * Soft-delete a user (ADMIN only).
 *
 * @param {number} id
 * @returns {Promise<void>}
 */
const deleteUser = async (id) => {
  await axiosInstance.delete(`${USERS_BASE}/${id}`);
};

/**
 * Toggle a user's active/inactive status (ADMIN only).
 *
 * @param {number} id
 * @returns {Promise<{ isActive: boolean }>}
 */
const toggleUserStatus = async (id) => {
  const response = await axiosInstance.patch(`${USERS_BASE}/${id}/toggle-status`);
  return response.data.data;
};

/**
 * Change a user's password.
 * ADMIN can change any user; regular users can only change their own.
 *
 * @param {number} id
 * @param {string} oldPassword - Current password (required for non-admin callers)
 * @param {string} newPassword
 * @returns {Promise<void>}
 */
const changePassword = async (id, oldPassword, newPassword) => {
  await axiosInstance.post(`${USERS_BASE}/${id}/change-password`, {
    oldPassword,
    newPassword,
  });
};

/**
 * Reset a user's password to a system-generated temporary value (ADMIN only).
 *
 * @param {number} id
 * @returns {Promise<{ temporaryPassword: string }>}
 */
const resetPassword = async (id) => {
  const response = await axiosInstance.patch(`${USERS_BASE}/${id}/reset-password`);
  return response.data.data;
};

// ─── Export ───────────────────────────────────────────────────────────────────

const userApi = {
  getAllUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  toggleUserStatus,
  changePassword,
  resetPassword,
};

export default userApi;