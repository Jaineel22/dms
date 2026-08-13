import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Search } from 'lucide-react';
import hierarchyApi from '../../api/hierarchyApi';

export default function ManagerAssignment() {
  const [searchParams] = useSearchParams();
  const preselectedUserId = searchParams.get('userId') || '';

  const [searchTerm, setSearchTerm] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const {
    register, handleSubmit, reset, formState: { errors },
  } = useForm({
    defaultValues: { userId: preselectedUserId, managerId: '', reason: '' },
  });

  const onAssignOrUpdate = async (values) => {
    if (!values.userId || !values.managerId) {
      toast.error('User and manager are both required');
      return;
    }
    setSubmitting(true);
    try {
      await hierarchyApi.assignManager({
        userId: Number(values.userId),
        managerId: Number(values.managerId),
        reason: values.reason,
      });
      toast.success('Manager assigned');
      reset({ userId: '', managerId: '', reason: '' });
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Assignment failed');
    } finally {
      setSubmitting(false);
    }
  };

  const onRemove = async (values) => {
    if (!values.userId) {
      toast.error('User id is required to remove a manager');
      return;
    }
    setSubmitting(true);
    try {
      await hierarchyApi.removeManager(Number(values.userId), values.reason);
      toast.success('Manager removed');
      reset({ userId: '', managerId: '', reason: '' });
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Removal failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Manager Assignment</h1>

      <div className="relative">
        <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
        <input
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          placeholder="Search users by name or email..."
          className="w-full rounded-md border border-gray-300 py-2 pl-9 pr-3 text-sm"
        />
        <p className="mt-1 text-xs text-gray-400">
          User search results wire up to your existing user-lookup endpoint/component — not duplicated here.
        </p>
      </div>

      <form className="space-y-4 rounded-lg border border-gray-200 bg-white p-6">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">User ID</label>
          <input
            type="number"
            {...register('userId', { required: 'User is required' })}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
          {errors.userId && <p className="mt-1 text-xs text-red-600">{errors.userId.message}</p>}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Manager ID</label>
          <input
            type="number"
            {...register('managerId')}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Reason</label>
          <textarea {...register('reason')} rows={3} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
        </div>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            disabled={submitting}
            onClick={handleSubmit(onRemove)}
            className="rounded-md border border-red-300 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-50"
          >
            Remove Manager
          </button>
          <button
            type="button"
            disabled={submitting}
            onClick={handleSubmit(onAssignOrUpdate)}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Saving...' : 'Assign / Update Manager'}
          </button>
        </div>
      </form>
    </div>
  );
}