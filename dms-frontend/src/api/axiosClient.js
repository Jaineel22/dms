// ─────────────────────────────────────────────────────────────────────────────
// axiosClient.js
// Compatibility re-export.
//
// Several API modules (approvalApi, auditApi, dashboardApi, documentApi,
// hierarchyApi, notificationApi, workflowApi) import the shared Axios instance
// as `./axiosClient`. The actual implementation lives in `./axios.js`.
// This file re-exports it so both import paths resolve to the same instance
// (with its JWT request interceptor and 401 response handler).
// ─────────────────────────────────────────────────────────────────────────────

export { default } from './axios';
