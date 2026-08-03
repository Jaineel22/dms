import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import documentApi from '../../api/documentApi';

export default function DocumentEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const {
    register, handleSubmit, reset, formState: { errors },
  } = useForm();

  useEffect(() => {
    (async () => {
      try {
        const doc = await documentApi.getDocumentById(id);
        reset({
          title: doc.title,
          description: doc.description || '',
          category: doc.category || '',
          department: doc.department || '',
          tags: doc.tags || '',
          confidential: !!doc.confidential,
          expiryDate: doc.expiryDate ? doc.expiryDate.substring(0, 10) : '',
        });
      } catch {
        toast.error('Failed to load document');
      } finally {
        setLoading(false);
      }
    })();
  }, [id, reset]);

  const onSubmit = async (values) => {
    setSubmitting(true);
    try {
      await documentApi.updateDocument(id, values);
      toast.success('Document updated');
      navigate(`/documents/${id}`);
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Update failed');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Edit Document</h1>
      <p className="text-sm text-gray-500">Only metadata can be updated here — the file itself cannot be changed.</p>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 rounded-lg border border-gray-200 bg-white p-6">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Title</label>
          <input {...register('title', { required: 'Title is required' })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          {errors.title && <p className="mt-1 text-xs text-red-600">{errors.title.message}</p>}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Description</label>
          <textarea {...register('description')} rows={3} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Category</label>
            <select {...register('category', { required: 'Category is required' })} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm">
              <option value="">Select category</option>
              <option value="Purchase">Purchase</option>
              <option value="Invoice">Invoice</option>
              <option value="HR">HR</option>
              <option value="Finance">Finance</option>
              <option value="Legal">Legal</option>
              <option value="General">General</option>
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Department</label>
            <input {...register('department')} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Tags</label>
            <input {...register('tags')} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Expiry Date</label>
            <input type="date" {...register('expiryDate')} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          </div>
        </div>

        <label className="flex items-center gap-2 text-sm text-gray-700">
          <input type="checkbox" {...register('confidential')} className="h-4 w-4 rounded border-gray-300" />
          Mark as confidential
        </label>

        <div className="flex justify-end gap-3">
          <button type="button" onClick={() => navigate(`/documents/${id}`)} className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
            Cancel
          </button>
          <button type="submit" disabled={submitting} className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
            {submitting ? 'Saving...' : 'Save Changes'}
          </button>
        </div>
      </form>
    </div>
  );
}