// Paths here match the ApiConstants generated in Phase 5(c) exactly.
import api from './axiosClient';

const BASE = '/audit';

export const auditApi = {
  getAuditLogs: (page = 0, size = 20) =>
    api.get(BASE, { params: { page, size } }).then((res) => res.data.data),

  getAuditLogsByUser: (userId, page = 0, size = 20) =>
    api.get(`${BASE}/user/${userId}`, { params: { page, size } }).then((res) => res.data.data),

  getAuditLogsByEntity: (entityType, entityId, page = 0, size = 20) =>
    api.get(`${BASE}/entity/${entityType}/${entityId}`, { params: { page, size } })
      .then((res) => res.data.data),

  getAuditLogsByAction: (action, page = 0, size = 20) =>
    api.get(`${BASE}/action/${action}`, { params: { page, size } }).then((res) => res.data.data),

  getRecentActivity: (limit = 20) =>
    api.get(`${BASE}/recent`, { params: { limit } }).then((res) => res.data.data),
};

export default auditApi;