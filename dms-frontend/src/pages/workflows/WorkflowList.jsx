import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Plus, Pencil, Trash2, Users } from 'lucide-react';
import workflowApi from '../../api/workflowApi';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';

export default function WorkflowList() {
  const navigate = useNavigate();
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(true);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await workflowApi.getAllWorkflows(pageNumber, 20);
      setPage(data);
    } catch {
      toast.error('Failed to load workflows');
    } finally {
      setLoading(false);
    }
  }, [pageNumber]);

  useEffect(() => { load(); }, [load]);

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await workflowApi.deleteWorkflow(deleteTarget.id);
      toast.success('Workflow deleted');
      setDeleteTarget(null);
      load();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Delete failed');
    } finally {
      setDeleting(false);
    }
  };

  const workflows = page?.content || [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-900">Workflow Definitions</h1>
        <button
          onClick={() => navigate('/workflows/new')}
          className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          <Plus className="h-4 w-4" /> Create Workflow
        </button>
      </div>

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50">
            <tr>
              {['Name', 'Description', 'Steps', 'Assigned Users', 'Status', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-medium text-gray-500">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {loading && <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>}
            {!loading && workflows.length === 0 && (
              <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">No workflows found</td></tr>
            )}
            {!loading && workflows.map((wf) => (
              <tr key={wf.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{wf.name}</td>
                <td className="px-4 py-3 text-gray-600">{wf.description}</td>
                <td className="px-4 py-3 text-gray-600">{wf.steps?.length ?? wf.stepCount ?? 0}</td>
                <td className="px-4 py-3 text-gray-600">{wf.assignedUserCount ?? 0}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${wf.isActive ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {wf.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <button title="Assign users" onClick={() => navigate(`/workflows/${wf.id}/assign`)} className="text-gray-500 hover:text-blue-600">
                      <Users className="h-4 w-4" />
                    </button>
                    <button title="Edit" onClick={() => navigate(`/workflows/${wf.id}/edit`)} className="text-gray-500 hover:text-blue-600">
                      <Pencil className="h-4 w-4" />
                    </button>
                    <button title="Delete" onClick={() => setDeleteTarget(wf)} className="text-gray-500 hover:text-red-600">
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <Pagination page={page} onPageChange={setPageNumber} />
      </div>

      <ConfirmDialog
        open={!!deleteTarget}
        title="Delete workflow?"
        message={deleteTarget ? `This will permanently delete "${deleteTarget.name}".` : ''}
        danger
        loading={deleting}
        confirmLabel="Delete"
        onConfirm={confirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
}