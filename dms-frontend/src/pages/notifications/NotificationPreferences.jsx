import { Fragment, useEffect, useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import toast from 'react-hot-toast';
import notificationApi from '../../api/notificationApi';

const TYPE_TOGGLES = [
  { key: 'approvalRequired', label: 'Approval Required' },
  { key: 'approved', label: 'Approved' },
  { key: 'rejected', label: 'Rejected' },
  { key: 'commented', label: 'Commented' },
  { key: 'escalated', label: 'Escalated' },
];

function Toggle({ checked, onChange, disabled }) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={() => onChange(!checked)}
      className={`relative h-5 w-9 rounded-full transition disabled:opacity-40 ${checked ? 'bg-blue-600' : 'bg-gray-300'}`}
    >
      <span className={`absolute top-0.5 h-4 w-4 rounded-full bg-white transition ${checked ? 'left-4' : 'left-0.5'}`} />
    </button>
  );
}

export default function NotificationPreferences() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const { control, handleSubmit, reset, watch } = useForm();

  useEffect(() => {
    (async () => {
      try {
        const prefs = await notificationApi.getPreferences();
        reset(prefs);
      } catch {
        toast.error('Failed to load preferences');
      } finally {
        setLoading(false);
      }
    })();
  }, [reset]);

  const emailEnabled = watch('emailEnabled');
  const inAppEnabled = watch('inAppEnabled');

  const onSubmit = async (values) => {
    setSaving(true);
    try {
      const updated = await notificationApi.updatePreferences(values);
      reset(updated);
      toast.success('Preferences saved');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Save failed');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Notification Preferences</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 rounded-lg border border-gray-200 bg-white p-6">
        <div className="flex items-center justify-between border-b border-gray-100 pb-4">
          <span className="text-sm font-medium text-gray-700">Email Notifications</span>
          <Controller name="emailEnabled" control={control} render={({ field }) => (
            <Toggle checked={!!field.value} onChange={field.onChange} />
          )} />
        </div>
        <div className="flex items-center justify-between border-b border-gray-100 pb-4">
          <span className="text-sm font-medium text-gray-700">In-App Notifications</span>
          <Controller name="inAppEnabled" control={control} render={({ field }) => (
            <Toggle checked={!!field.value} onChange={field.onChange} />
          )} />
        </div>

        <div className="space-y-3">
          <p className="text-sm font-medium text-gray-700">Per-type preferences</p>
          <div className="grid grid-cols-3 gap-y-3 text-sm text-gray-600">
            <span />
            <span className="text-center text-xs font-medium text-gray-400">Email</span>
            <span className="text-center text-xs font-medium text-gray-400">In-App</span>
            {TYPE_TOGGLES.map(({ key, label }) => (
              <Fragment key={key}>
                <span className="py-1">{label}</span>
                <span className="flex justify-center py-1">
                  <Controller name={`${key}Email`} control={control} render={({ field }) => (
                    <Toggle checked={!!field.value} onChange={field.onChange} disabled={!emailEnabled} />
                  )} />
                </span>
                <span className="flex justify-center py-1">
                  <Controller name={`${key}InApp`} control={control} render={({ field }) => (
                    <Toggle checked={!!field.value} onChange={field.onChange} disabled={!inAppEnabled} />
                  )} />
                </span>
              </Fragment>
            ))}
          </div>
        </div>

        <div className="flex justify-end">
          <button type="submit" disabled={saving} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
            {saving ? 'Saving...' : 'Save Preferences'}
          </button>
        </div>
      </form>
    </div>
  );
}