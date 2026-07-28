// Paths here match the ApiConstants generated in Phase 5(c) exactly.
import api from './axiosClient';

const BASE = '/notifications';

export const notificationApi = {
  getNotifications: (page = 0, size = 20) =>
    api.get(BASE, { params: { page, size } }).then((res) => res.data.data),

  getUnreadNotifications: (page = 0, size = 20) =>
    api.get(`${BASE}/unread`, { params: { page, size } }).then((res) => res.data.data),

  getNotificationSummary: () =>
    api.get(`${BASE}/summary`).then((res) => res.data.data),

  markAsRead: (id) =>
    api.put(`${BASE}/${id}/read`).then((res) => res.data),

  markAllAsRead: () =>
    api.put(`${BASE}/read-all`).then((res) => res.data),

  deleteNotification: (id) =>
    api.delete(`${BASE}/${id}`).then((res) => res.data),

  getPreferences: () =>
    api.get(`${BASE}/preferences`).then((res) => res.data.data),

  updatePreferences: (data) =>
    api.put(`${BASE}/preferences`, data).then((res) => res.data.data),
};

export default notificationApi;