// NOTE: Hierarchy endpoint paths are assumed — Phase 2 (hierarchy backend) was generated
// in a separate session, so these mirror common REST conventions. Adjust if your actual
// backend routes differ.
import api from './axiosClient';

const BASE = '/hierarchy';

export const hierarchyApi = {
  assignManager: (data) =>
    api.post(`${BASE}/assign-manager`, data).then((res) => res.data.data),

  updateManager: (userId, managerId, reason) =>
    api.put(`${BASE}/${userId}/manager`, { managerId, reason }).then((res) => res.data.data),

  removeManager: (userId, reason) =>
    api.delete(`${BASE}/${userId}/manager`, { data: { reason } }).then((res) => res.data),

  getReportingHierarchy: (userId) =>
    api.get(`${BASE}/${userId}`).then((res) => res.data.data),

  getTeamMembers: (managerId) =>
    api.get(`${BASE}/${managerId}/team`).then((res) => res.data.data),

  getDirectReports: (managerId) =>
    api.get(`${BASE}/${managerId}/direct-reports`).then((res) => res.data.data),

  getReportingChain: (userId) =>
    api.get(`${BASE}/${userId}/chain`).then((res) => res.data.data),
};

export default hierarchyApi;