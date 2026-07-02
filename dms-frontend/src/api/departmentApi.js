// ─────────────────────────────────────────────────────────────────────────────
// departmentApi.js
// Department management API functions matching all 6 DepartmentController endpoints.
// ─────────────────────────────────────────────────────────────────────────────

import axiosInstance from './axios';

// ─── Endpoints ────────────────────────────────────────────────────────────────

const DEPTS_BASE = '/departments';

// ─── Functions ────────────────────────────────────────────────────────────────

/**
 * Fetch paginated list of active departments.
 *
 * @param {Object} params
 * @param {number} [params.page=0]          - Zero-based page number
 * @param {number} [params.size=20]         - Page size
 * @param {string} [params.sort='name,asc'] - Sort field and direction
 * @returns {Promise<Page<DepartmentResponse>>}
 */
const getAllDepartments = async ({
  page = 0,
  size = 20,
  sort = 'name,asc',
} = {}) => {
  const response = await axiosInstance.get(DEPTS_BASE, {
    params: { page, size, sort },
  });
  return response.data.data;
};

/**
 * Fetch a single department by ID, including its active user count.
 *
 * @param {number} id
 * @returns {Promise<DepartmentResponse>}
 */
const getDepartmentById = async (id) => {
  const response = await axiosInstance.get(`${DEPTS_BASE}/${id}`);
  return response.data.data;
};

/**
 * Create a new department (ADMIN only).
 *
 * @param {Object} data - DepartmentRequest payload { name, code, description }
 * @returns {Promise<DepartmentResponse>}
 */
const createDepartment = async (data) => {
  const response = await axiosInstance.post(DEPTS_BASE, data);
  return response.data.data;
};

/**
 * Update an existing department (ADMIN only).
 *
 * @param {number} id
 * @param {Object} data - DepartmentRequest payload { name, code, description }
 * @returns {Promise<DepartmentResponse>}
 */
const updateDepartment = async (id, data) => {
  const response = await axiosInstance.put(`${DEPTS_BASE}/${id}`, data);
  return response.data.data;
};

/**
 * Delete a department (ADMIN only).
 * Returns 422 if the department still has active users assigned.
 *
 * @param {number} id
 * @returns {Promise<void>}
 */
const deleteDepartment = async (id) => {
  await axiosInstance.delete(`${DEPTS_BASE}/${id}`);
};

/**
 * Fetch all active users assigned to a department (ADMIN only).
 *
 * @param {number} id - Department ID
 * @returns {Promise<UserResponse[]>}
 */
const getDepartmentUsers = async (id) => {
  const response = await axiosInstance.get(`${DEPTS_BASE}/${id}/users`);
  return response.data.data;
};

// ─── Export ───────────────────────────────────────────────────────────────────

const departmentApi = {
  getAllDepartments,
  getDepartmentById,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  getDepartmentUsers,
};

export default departmentApi;