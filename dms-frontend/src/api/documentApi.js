// NOTE: Document endpoint paths (e.g. /documents, /documents/{id}/archive) are assumed —
// Phase 3 (document management backend) was generated in a separate session, so these
// mirror common REST conventions rather than confirmed ApiConstants values. Adjust the
// path strings below if your actual backend routes differ.
import api from './axiosClient';

const BASE = '/documents';

export const documentApi = {
  uploadDocument: (formData) =>
    api.post(BASE, formData, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((res) => res.data.data),

  getAllDocuments: (page = 0, size = 20, sort = 'createdAt,desc', search = '') =>
    api.get(BASE, { params: { page, size, sort, search: search || undefined } })
      .then((res) => res.data.data),

  getDocumentById: (id) =>
    api.get(`${BASE}/${id}`).then((res) => res.data.data),

  getDocumentByNumber: (number) =>
    api.get(`${BASE}/number/${number}`).then((res) => res.data.data),

  updateDocument: (id, data) =>
    api.put(`${BASE}/${id}`, data).then((res) => res.data.data),

  deleteDocument: (id) =>
    api.delete(`${BASE}/${id}`).then((res) => res.data),

  archiveDocument: (id) =>
    api.put(`${BASE}/${id}/archive`).then((res) => res.data.data),

  restoreDocument: (id) =>
    api.put(`${BASE}/${id}/restore`).then((res) => res.data.data),

  downloadDocument: (id) =>
    api.get(`${BASE}/${id}/download`, { responseType: 'blob' }).then((res) => res.data),

  downloadDocumentVersion: (id, version) =>
    api.get(`${BASE}/${id}/versions/${version}/download`, { responseType: 'blob' })
      .then((res) => res.data),

  getDocumentVersions: (id) =>
    api.get(`${BASE}/${id}/versions`).then((res) => res.data.data),

  advancedSearch: (filters) =>
    api.post(`${BASE}/search`, filters).then((res) => res.data.data),

  getDocumentSummary: () =>
    api.get(`${BASE}/summary`).then((res) => res.data.data),
};

export default documentApi;