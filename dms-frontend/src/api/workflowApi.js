// NOTE: Workflow-definition endpoint paths are assumed — Phase 2 (hierarchy & workflow
// definitions backend) was generated in a separate session. This is distinct from
// approvalApi.js, which covers Phase 4 workflow EXECUTION (submit/approve/reject).
import api from './axiosClient';

const BASE = '/workflows';

export const workflowApi = {
  getAllWorkflows: (page = 0, size = 20, sort = 'name,asc') =>
    api.get(BASE, { params: { page, size, sort } }).then((res) => res.data.data),

  getWorkflowById: (id) =>
    api.get(`${BASE}/${id}`).then((res) => res.data.data),

  createWorkflow: (data) =>
    api.post(BASE, data).then((res) => res.data.data),

  updateWorkflow: (id, data) =>
    api.put(`${BASE}/${id}`, data).then((res) => res.data.data),

  deleteWorkflow: (id) =>
    api.delete(`${BASE}/${id}`).then((res) => res.data),

  getWorkflowsByDepartment: (departmentId) =>
    api.get(`${BASE}/department/${departmentId}`).then((res) => res.data.data),

  assignWorkflowToUser: (workflowId, userId) =>
    api.post(`${BASE}/${workflowId}/users/${userId}`).then((res) => res.data.data),

  removeWorkflowFromUser: (workflowId, userId) =>
    api.delete(`${BASE}/${workflowId}/users/${userId}`).then((res) => res.data),

  getUserWorkflows: (userId) =>
    api.get(`${BASE}/user/${userId}`).then((res) => res.data.data),
};

export default workflowApi;