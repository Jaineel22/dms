// Paths here match the ApiConstants generated in Phase 4(c) exactly.
import api from './axiosClient';

const WORKFLOW_BASE = '/workflow';
const APPROVALS_BASE = '/approvals';

const toFormData = (data = {}) => {
  const formData = new FormData();
  Object.entries(data).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      formData.append(key, value);
    }
  });
  return formData;
};

export const approvalApi = {
  submitDocument: (data) =>
    api.post(`${WORKFLOW_BASE}/submit`, data).then((res) => res.data.data),

  getPendingApprovals: (page = 0, size = 20) =>
    api.get(`${WORKFLOW_BASE}/pending`, { params: { page, size } }).then((res) => res.data.data),

  approveApproval: (id, data) =>
    api.post(`${APPROVALS_BASE}/${id}/approve`, toFormData(data), {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((res) => res.data.data),

  rejectApproval: (id, data) =>
    api.post(`${APPROVALS_BASE}/${id}/reject`, toFormData(data), {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((res) => res.data.data),

  sendBackApproval: (id, data) =>
    api.post(`${APPROVALS_BASE}/${id}/send-back`, toFormData(data), {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then((res) => res.data.data),

  getApprovalHistory: (documentId) =>
    api.get(`${WORKFLOW_BASE}/history/${documentId}`).then((res) => res.data.data),

  // Resolves the current (actionable) approval for a workflow instance. Needed because
  // PendingApprovalResponse only exposes the workflow instance id, while approve/reject/
  // send-back act on a specific approval id — see ApprovalAction.jsx for the lookup flow.
  getCurrentApproval: (instanceId) =>
    api.get(`${APPROVALS_BASE}/current/${instanceId}`).then((res) => res.data.data),

  getWorkflowInstance: (id) =>
    api.get(`${WORKFLOW_BASE}/instances/${id}`).then((res) => res.data.data),

  escalateApproval: (id, data) =>
    api.post(`${APPROVALS_BASE}/${id}/escalate`, null, {
      params: { reason: data.reason, toUserId: data.toUserId },
    }).then((res) => res.data.data),

  cancelWorkflow: (id) =>
    api.delete(`${WORKFLOW_BASE}/instances/${id}/cancel`).then((res) => res.data),

  getWorkflowSummary: () =>
    api.get(`${WORKFLOW_BASE}/summary`).then((res) => res.data.data),

  // ── Comments (Phase 4 CommentController) ──────────────────────────────────
  // Not part of the original 7-file API list, but ApprovalThread.jsx needs them
  // and no dedicated commentApi.js was specified, so they live here alongside
  // the rest of the approval-workflow surface.
  getDocumentComments: (documentId, page = 0, size = 20) =>
    api.get('/comments/document/' + documentId, { params: { page, size } }).then((res) => res.data.data),

  getCommentThread: (commentId) =>
    api.get('/comments/thread/' + commentId).then((res) => res.data.data),

  addComment: (data) =>
    api.post('/comments', data).then((res) => res.data.data),

  updateComment: (id, content) =>
    api.put('/comments/' + id, { content }).then((res) => res.data.data),

  deleteComment: (id) =>
    api.delete('/comments/' + id).then((res) => res.data),
};

export default approvalApi;