// Paths here match the ApiConstants generated in Phase 5(c) exactly.
import api from './axiosClient';

const BASE = '/dashboard';

export const dashboardApi = {
  getDashboardStats: () =>
    api.get(`${BASE}/stats`).then((res) => res.data.data),

  getUserDashboardStats: () =>
    api.get(`${BASE}/user-stats`).then((res) => res.data.data),

  getRecentActivity: (limit = 10) =>
    api.get(`${BASE}/recent-activity`, { params: { limit } }).then((res) => res.data.data),

  getDocumentStats: () =>
    api.get(`${BASE}/document-stats`).then((res) => res.data.data),

  getWorkflowStats: () =>
    api.get(`${BASE}/workflow-stats`).then((res) => res.data.data),
};

export default dashboardApi;