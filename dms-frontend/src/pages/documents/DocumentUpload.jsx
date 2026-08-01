import { useState, useCallback } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { UploadCloud, File as FileIcon, X } from 'lucide-react';
import documentApi from '../../api/documentApi';

const ALLOWED_TYPES = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'image/png', 'image/jpeg', 'text/plain'];
const MAX_SIZE = 10 * 1024 * 1024; // 10MB

export default function DocumentUpload() {
  const navigate = useNavigate();
  const [file, setFile] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [progress, setProgress] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  const {
    register, handleSubmit, control, formState: { errors },
  } = useForm({
    defaultValues: {
      title: '', description: '', category: '', department: '', tags: '', confidential: false, expiryDate: '',
    },
  });

  const validateAndSetFile = (selected) => {
    if (!selected) return;
    if (!ALLOWED_TYPES.includes(selected.type)) {
      toast.error('Unsupported file type. Allowed: PDF, DOCX, XLSX, PNG, JPEG, TXT');
      return;
    }
    if (selected.size > MAX_SIZE) {
      toast.error('File exceeds the 10MB limit');
      return;
    }
    setFile(selected);
  };

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    setDragActive(false);
    validateAndSetFile(e.dataTransfer.files?.[0]);
  }, []);

  const onSubmit = async (values) => {
    if (!file) {
      toast.error('Please select a file to upload');
      return;
    }
    setSubmitting(true);
    setProgress(0);
    try {
      const formData = new FormData();
      formData.append('file', file);
      Object.entries(values).forEach(([key, value]) => {
        if (value !== '' && value !== undefined && value !== null) formData.append(key, value);
      });

      const doc = await documentApi.uploadDocument(formData);
      toast.success('Document uploaded successfully');
      navigate(doc?.id ? `/documents/${doc.id}` : '/documents');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Upload failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Upload Document</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 rounded-lg border border-gray-200 bg-white p-6">
        <div
          onDragOver={(e) => { e.preventDefault(); setDragActive(true); }}
          onDragLeave={() => setDragActive(false)}
          onDrop={handleDrop}
          className={`flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-8 text-center transition ${
            dragActive ? 'border-blue-500 bg-blue-50' : 'border-gray-300'
          }`}
        >
          {file ? (
            <div className="flex items-center gap-3">
              <FileIcon className="h-8 w-8 text-blue-600" />
              <div className="text-left">
                <p className="text-sm font-medium text-gray-900">{file.name}</p>
                <p className="text-xs text-gray-500">{(file.size / 1024).toFixed(1)} KB</p>
              </div>
              <button type="button" onClick={() => setFile(null)} className="text-gray-400 hover:text-red-500">
                <X className="h-4 w-4" />
              </button>
            </div>
          ) : (
            <>
              <UploadCloud className="mb-2 h-10 w-10 text-gray-400" />
              <p className="text-sm text-gray-600">Drag and drop a file here, or</p>
              <label className="mt-2 cursor-pointer text-sm font-medium text-blue-600 hover:underline">
                browse to upload
                <input
                  type="file"
                  className="hidden"
                  onChange={(e) => validateAndSetFile(e.target.files?.[0])}
                />
              </label>
              <p className="mt-2 text-xs text-gray-400">PDF, DOCX, XLSX, PNG, JPEG, TXT — max 10MB</p>
            </>
          )}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Title</label>
          <input
            {...register('title', { required: 'Title is required' })}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
          {errors.title && <p className="mt-1 text-xs text-red-600">{errors.title.message}</p>}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Description</label>
          <textarea
            {...register('description')}
            rows={3}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Category</label>
            <select
              {...register('category', { required: 'Category is required' })}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            >
              <option value="">Select category</option>
              <option value="Purchase">Purchase</option>
              <option value="Invoice">Invoice</option>
              <option value="HR">HR</option>
              <option value="Finance">Finance</option>
              <option value="Legal">Legal</option>
              <option value="General">General</option>
            </select>
            {errors.category && <p className="mt-1 text-xs text-red-600">{errors.category.message}</p>}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Department</label>
            <input
              {...register('department')}
              placeholder="Department ID or name"
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Tags (comma separated)</label>
            <input {...register('tags')} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-gray-700">Expiry Date</label>
            <input type="date" {...register('expiryDate')} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
          </div>
        </div>

        <Controller
          name="confidential"
          control={control}
          render={({ field }) => (
            <label className="flex items-center gap-2 text-sm text-gray-700">
              <input
                type="checkbox"
                checked={field.value}
                onChange={(e) => field.onChange(e.target.checked)}
                className="h-4 w-4 rounded border-gray-300"
              />
              Mark as confidential
            </label>
          )}
        />

        {submitting && (
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
            <div className="h-full animate-pulse bg-blue-600" style={{ width: `${progress || 60}%` }} />
          </div>
        )}

        <div className="flex justify-end gap-3">
          <button
            type="button"
            onClick={() => navigate('/documents')}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {submitting ? 'Uploading...' : 'Upload Document'}
          </button>
        </div>
      </form>
    </div>
  );
}