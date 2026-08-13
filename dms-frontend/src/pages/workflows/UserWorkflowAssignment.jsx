import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Trash2 } from 'lucide-react';
import workflowApi from '../../api/workflowApi';

export default function UserWorkflowAssignment() {
  const { id: workflowId } = useParams();
  const [workflow, setWorkflow] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const { register, handleSubmit, reset, formState: { errors } } = useForm();

  const load = async () => {
    setLoading(true);
    try {
      const wf = await workflowApi.getWorkflowById(workflowId);
      setWorkflow(wf);
    } catch {
      toast.error('Failed to load workflow');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [workflowId]);

  const onAssign = async (values) => {
    setSubmitting(true);
    try {
      await workflowApi.assignWorkflowToUser(workflowId, Number(values.userId));
      toast.success('User assigned to workflow');
      reset();
      load();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Assignment failed');
    } finally {
      setSubmitting(false);
    }
  };

  const removeUser = async (userId) => {
    try {
      await workflowApi.removeWorkflowFromUser(workflowId, userId);
      toast.success('Assignment removed');
      load();
    } catch {
      toast.error('Removal failed');
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  const assignedUsers = workflow?.assignedUsers || [];

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Assign Users — {workflow?.name}</h1>

      <form onSubmit={handleSubmit(onAssign)} className="flex items-end gap-3 rounded-lg border border-gray-200 bg-white p-4">
        <div className="flex-1">
          <label className="mb-1 block text-sm font-medium text-gray-700">User ID</label>
          <input type="number" {...register('userId', { required: 'User is required' })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          {errors.userId && <p className="mt-1 text-xs text-red-600">{errors.userId.message}</p>}
        </div>
        <button type="submit" disabled={submitting} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
          Assign
        </button>
      </form>

      <div className="rounded-lg border border-gray-200 bg-white">
        <ul className="divide-y divide-gray-100">
          {assignedUsers.length === 0 && <li className="px-4 py-6 text-center text-sm text-gray-400">No users assigned yet</li>}
          {assignedUsers.map((u) => (
            <li key={u.userId || u.id} className="flex items-center justify-between px-4 py-3 text-sm">
              <span>{u.fullName || u.name} <span className="text-gray-400">({u.email})</span></span>
              <button onClick={() => removeUser(u.userId || u.id)} className="text-red-600 hover:text-red-700">
                <Trash2 className="h-4 w-4" />
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}