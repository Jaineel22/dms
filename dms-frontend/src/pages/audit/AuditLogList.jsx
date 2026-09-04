import { useEffect, useState, useCallback } from 'react';
import toast from 'react-hot-toast';
import auditApi from '../../api/auditApi';
import Pagination from '../../components/common/Pagination';

const ACTIONS = [
  'CREATE', 'UPDATE', 'DELETE', 'VIEW', 'UPLOAD', 'DOWNLOAD', 'SUBMIT', 'APPROVE', 'REJECT',
  'SEND_BACK', 'ESCALATE', 'ARCHIVE', 'RESTORE', 'LOGIN', 'LOGOUT', 'CHANGE_PASSWORD',
  'RESET_PASSWORD', 'ASSIGN_MANAGER', 'UPDATE_MANAGER',
];
const ENTITY_TYPES = [
  'USER', 'DEPARTMENT', 'DOCUMENT', 'DOCUMENT_VERSION', 'WORKFLOW_DEFINITION',
  'WORKFLOW_INSTANCE', 'APPROVAL', 'COMMENT', 'HIERARCHY', 'NOTIFICATION',
];

export default function AuditLogList() {
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ userId: '', action: '', entityType: '', entityId: '' });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      let data;
      if (filters.userId) {
        data = await auditApi.getAuditLogsByUser(filters.userId, pageNumber, 20);
      } else if (filters.entityType && filters.entityId) {
        data = await auditApi.getAuditLogsByEntity(filters.entityType, filters.entityId, pageNumber, 20);
      } else if (filters.action) {
        data = await auditApi.getAuditLogsByAction(filters.action, pageNumber, 20);
      } else {
        data = await auditApi.getAuditLogs(pageNumber, 20);
      }
      setPage(data);
    } catch {
      toast.error('Failed to load audit logs');
    } finally {
      setLoading(false);
    }
  }, [pageNumber, filters]);

  useEffect(() => { load(); }, [load]);

  const logs = page?.content || [];

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-semibold text-gray-900">Audit Logs</h1>

      <div className="flex flex-wrap gap-3 rounded-lg border border-gray-200 bg-white p-4">
        <input
          placeholder="Filter by User ID"
          value={filters.userId}
          onChange={(e) => setFilters((f) => ({ ...f, userId: e.target.value }))}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
        <select
          value={filters.action}
          onChange={(e) => setFilters((f) => ({ ...f, action: e.target.value }))}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        >
          <option value="">Any Action</option>
          {ACTIONS.map((a) => <option key={a} value={a}>{a}</option>)}
        </select>
        <select
          value={filters.entityType}
          onChange={(e) => setFilters((f) => ({ ...f, entityType: e.target.value }))}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        >
          <option value="">Any Entity Type</option>
          {ENTITY_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
        </select>
        <input
          placeholder="Entity ID"
          value={filters.entityId}
          onChange={(e) => setFilters((f) => ({ ...f, entityId: e.target.value }))}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
        <button
          onClick={() => { setPageNumber(0); load(); }}
          className="rounded-md bg-gray-900 px-4 py-2 text-sm font-medium text-white hover:bg-gray-800"
        >
          Apply Filters
        </button>
      </div>

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50">
            <tr>
              {['User', 'Action', 'Entity', 'Old Value', 'New Value', 'IP', 'Timestamp'].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-medium text-gray-500">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {loading && <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>}
            {!loading && logs.length === 0 && (
              <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">No audit logs found</td></tr>
            )}
            {!loading && logs.map((log) => (
              <tr key={log.id} className="align-top hover:bg-gray-50">
                <td className="px-4 py-3">
                  <div className="text-gray-900">{log.userFullName || 'Unknown User'}</div>
                  <div className="text-xs text-gray-400">{log.userEmail}</div>
                </td>
                <td className="px-4 py-3 text-gray-600">{log.action}</td>
                <td className="px-4 py-3 text-gray-600">{log.entityType} #{log.entityId}</td>
                <td className="px-4 py-3 max-w-[160px] truncate text-xs text-gray-500" title={JSON.stringify(log.oldValue)}>
                  {log.oldValue ? JSON.stringify(log.oldValue) : '-'}
                </td>
                <td className="px-4 py-3 max-w-[160px] truncate text-xs text-gray-500" title={JSON.stringify(log.newValue)}>
                  {log.newValue ? JSON.stringify(log.newValue) : '-'}
                </td>
                <td className="px-4 py-3 text-gray-500">{log.ipAddress || '-'}</td>
                <td className="px-4 py-3 text-gray-500">{log.createdAt ? new Date(log.createdAt).toLocaleString() : '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <Pagination
          page={page?.number ?? pageNumber}
          totalPages={page?.totalPages ?? 0}
          totalElements={page?.totalElements ?? 0}
          size={page?.size ?? 20}
          onPageChange={setPageNumber}
        />
      </div>
    </div>
  );
}