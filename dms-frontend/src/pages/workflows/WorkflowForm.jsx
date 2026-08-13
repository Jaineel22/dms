import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Plus } from 'lucide-react';
import workflowApi from '../../api/workflowApi';
import WorkflowStepForm from './WorkflowStepForm';

export default function WorkflowForm() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();

  const [steps, setSteps] = useState([]);
  const [stepModal, setStepModal] = useState(null); // { step, index } or { step: null } for new
  const [loading, setLoading] = useState(isEdit);
  const [submitting, setSubmitting] = useState(false);

  const {
    register, handleSubmit, reset, formState: { errors },
  } = useForm({ defaultValues: { name: '', description: '', department: '' } });

  useEffect(() => {
    if (!isEdit) return;
    (async () => {
      try {
        const wf = await workflowApi.getWorkflowById(id);
        reset({ name: wf.name, description: wf.description || '', department: wf.department || '' });
        setSteps(wf.steps || []);
      } catch {
        toast.error('Failed to load workflow');
      } finally {
        setLoading(false);
      }
    })();
  }, [id, isEdit, reset]);

  const saveStep = (step, index) => {
    setSteps((prev) => {
      const next = [...prev];
      if (index === undefined || index === null) {
        next.push({ ...step, stepNumber: next.length + 1 });
      } else {
        next[index] = step;
      }
      return next;
    });
    setStepModal(null);
  };

  const removeStep = (index) => {
    setSteps((prev) => prev.filter((_, i) => i !== index).map((s, i) => ({ ...s, stepNumber: i + 1 })));
  };

  const onSubmit = async (values) => {
    if (steps.length === 0) {
      toast.error('Add at least one approval step');
      return;
    }
    setSubmitting(true);
    try {
      const payload = { ...values, steps };
      if (isEdit) {
        await workflowApi.updateWorkflow(id, payload);
        toast.success('Workflow updated');
      } else {
        await workflowApi.createWorkflow(payload);
        toast.success('Workflow created');
      }
      navigate('/workflows');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Save failed');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">{isEdit ? 'Edit Workflow' : 'Create Workflow'}</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 rounded-lg border border-gray-200 bg-white p-6">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Name</label>
          <input {...register('name', { required: 'Name is required' })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          {errors.name && <p className="mt-1 text-xs text-red-600">{errors.name.message}</p>}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Description</label>
          <textarea {...register('description')} rows={2} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Department</label>
          <input {...register('department')} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
        </div>

        <div>
          <div className="mb-2 flex items-center justify-between">
            <label className="text-sm font-medium text-gray-700">Approval Steps</label>
            <button
              type="button"
              onClick={() => setStepModal({ step: null, index: null })}
              className="inline-flex items-center gap-1.5 text-sm text-blue-600 hover:underline"
            >
              <Plus className="h-4 w-4" /> Add Step
            </button>
          </div>

          {steps.length === 0 ? (
            <p className="text-sm text-gray-400">No steps added yet</p>
          ) : (
            <ul className="space-y-2">
              {steps.map((s, idx) => (
                <li key={idx} className="flex items-center justify-between rounded-md border border-gray-200 px-3 py-2 text-sm">
                  <span>
                    Step {s.stepNumber} — Level {s.approvalLevel} — {s.role}
                    {s.mandatory === false && <span className="ml-2 text-xs text-gray-400">(optional)</span>}
                    {s.timeoutHours ? <span className="ml-2 text-xs text-gray-400">timeout {s.timeoutHours}h</span> : null}
                  </span>
                  <span className="flex items-center gap-3">
                    <button type="button" onClick={() => setStepModal({ step: s, index: idx })} className="text-blue-600 hover:underline">
                      Edit
                    </button>
                    <button type="button" onClick={() => removeStep(idx)} className="text-red-600 hover:underline">
                      Remove
                    </button>
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={() => navigate('/workflows')} className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
            Cancel
          </button>
          <button type="submit" disabled={submitting} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
            {submitting ? 'Saving...' : 'Save Workflow'}
          </button>
        </div>
      </form>

      {stepModal && (
        <WorkflowStepForm
          step={stepModal.step}
          nextStepNumber={steps.length + 1}
          onSave={(step) => saveStep(step, stepModal.index)}
          onClose={() => setStepModal(null)}
        />
      )}
    </div>
  );
}