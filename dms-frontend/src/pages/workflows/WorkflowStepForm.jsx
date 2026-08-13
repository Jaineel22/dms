import { useForm } from 'react-hook-form';
import { X } from 'lucide-react';

const ROLES = ['MANAGER', 'FINANCE', 'LEGAL', 'HR', 'ADMIN'];

export default function WorkflowStepForm({ step, nextStepNumber, onSave, onClose }) {
  const {
    register, handleSubmit, formState: { errors },
  } = useForm({
    defaultValues: {
      stepNumber: step?.stepNumber ?? nextStepNumber,
      approvalLevel: step?.approvalLevel ?? 1,
      role: step?.role ?? 'MANAGER',
      mandatory: step?.mandatory ?? true,
      timeoutHours: step?.timeoutHours ?? 48,
    },
  });

  const onSubmit = (values) => {
    onSave({
      stepNumber: Number(values.stepNumber),
      approvalLevel: Number(values.approvalLevel),
      role: values.role,
      mandatory: !!values.mandatory,
      timeoutHours: values.timeoutHours ? Number(values.timeoutHours) : null,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold text-gray-900">{step ? 'Edit Step' : 'Add Step'}</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X className="h-5 w-5" /></button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="mt-4 space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Step Number</label>
            <input type="number" min={1} {...register('stepNumber', { required: true, min: 1 })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Approval Level (1-4)</label>
            <input type="number" min={1} max={4} {...register('approvalLevel', { required: true, min: 1, max: 4 })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
            {errors.approvalLevel && <p className="mt-1 text-xs text-red-600">Level must be between 1 and 4</p>}
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Role</label>
            <select {...register('role', { required: true })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm">
              {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Timeout (hours)</label>
            <input type="number" min={1} {...register('timeoutHours')} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          </div>

          <label className="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" {...register('mandatory')} className="h-4 w-4 rounded border-gray-300" />
            Mandatory step
          </label>

          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
              Cancel
            </button>
            <button type="submit" className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
              Save Step
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}