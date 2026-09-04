import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  Upload, Search, Download, Eye, Pencil, Archive, ArchiveRestore, Trash2, Send,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import documentApi from '../../api/documentApi';
import approvalApi from '../../api/approvalApi';
import Pagination from '../../components/common/Pagination';
import ConfirmDialog from '../../components/common/ConfirmDialog';

const STATUS_STYLES = {
  DRAFT: 'bg-gray-100 text-gray-700',
  UNDER_REVIEW: 'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
  CANCELLED: 'bg-gray-100 text-gray-500',
};

export default function DocumentList() {
  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  const [page, setPage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [pageNumber, setPageNumber] = useState(0);
  const [search, setSearch] = useState('');
  const [filters, setFilters] = useState({ category: '', department: '', status: '', from: '', to: '' });
  const [confirmTarget, setConfirmTarget] = useState(null); // { type: 'archive'|'restore'|'delete'|'submit', doc }
  const [actionLoading, setActionLoading] = useState(false);

  const loadDocuments = useCallback(async () => {
    setLoading(true);
    try {
      const data = await documentApi.getAllDocuments(pageNumber, 20, 'createdAt,desc', search);
      setPage(data);
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  }, [pageNumber, search]);

  useEffect(() => {
    loadDocuments();
  }, [loadDocuments]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPageNumber(0);
    loadDocuments();
  };

  const handleDownload = async (doc) => {
    try {
      const blob = await documentApi.downloadDocument(doc.id);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = doc.fileName || doc.title;
      link.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      toast.error('Download failed');
    }
  };

  const runConfirmedAction = async () => {
    if (!confirmTarget) return;
    setActionLoading(true);
    try {
      const { type, doc } = confirmTarget;
      if (type === 'archive') {
        await documentApi.archiveDocument(doc.id);
        toast.success('Document archived');
      } else if (type === 'restore') {
        await documentApi.restoreDocument(doc.id);
        toast.success('Document restored');
      } else if (type === 'delete') {
        await documentApi.deleteDocument(doc.id);
        toast.success('Document deleted');
      } else if (type === 'submit') {
        await approvalApi.submitDocument({ documentId: doc.id });
        toast.success('Document submitted for approval');
      }
      setConfirmTarget(null);
      loadDocuments();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Action failed');
    } finally {
      setActionLoading(false);
    }
  };

  const documents = page?.content || [];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-900">
          {isAdmin ? 'All Documents' : 'My Documents'}
        </h1>
        <Link
          to="/documents/upload"
          className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          <Upload className="h-4 w-4" /> Upload Document
        </Link>
      </div>

      <form onSubmit={handleSearchSubmit} className="flex flex-wrap gap-3 rounded-lg border border-gray-200 bg-white p-4">
        <div className="relative flex-1 min-w-[220px]">
          <Search className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by title, number, or tags..."
            className="w-full rounded-md border border-gray-300 py-2 pl-9 pr-3 text-sm focus:border-blue-500 focus:outline-none"
          />
        </div>
        <select
          value={filters.status}
          onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value }))}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        >
          <option value="">All Statuses</option>
          <option value="DRAFT">Draft</option>
          <option value="UNDER_REVIEW">Under Review</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
        </select>
        <input
          type="text"
          placeholder="Category"
          value={filters.category}
          onChange={(e) => setFilters((f) => ({ ...f, category: e.target.value }))}
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
        <button type="submit" className="rounded-md bg-gray-900 px-4 py-2 text-sm font-medium text-white hover:bg-gray-800">
          Search
        </button>
      </form>

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50">
            <tr>
              {['Number', 'Title', 'Category', 'Status', 'Version', 'Uploaded By', 'Created At', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-medium text-gray-500">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {loading && (
              <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>
            )}
            {!loading && documents.length === 0 && (
              <tr><td colSpan={8} className="px-4 py-8 text-center text-gray-400">No documents found</td></tr>
            )}
            {!loading && documents.map((doc) => (
              <tr key={doc.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-mono text-xs text-gray-600">{doc.documentNumber}</td>
                <td className="px-4 py-3">
                  <button onClick={() => navigate(`/documents/${doc.id}`)} className="font-medium text-blue-600 hover:underline">
                    {doc.title}
                  </button>
                </td>
                <td className="px-4 py-3 text-gray-600">{doc.category}</td>
                <td className="px-4 py-3">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[doc.workflowStatus] || 'bg-gray-100 text-gray-700'}`}>
                    {doc.workflowStatus}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-600">v{doc.currentVersion || 1}</td>
                <td className="px-4 py-3 text-gray-600">{doc.uploadedByName || doc.uploadedBy}</td>
                <td className="px-4 py-3 text-gray-600">{doc.createdAt ? new Date(doc.createdAt).toLocaleDateString() : '-'}</td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <button title="View" onClick={() => navigate(`/documents/${doc.id}`)} className="text-gray-500 hover:text-blue-600">
                      <Eye className="h-4 w-4" />
                    </button>
                    <button title="Download" onClick={() => handleDownload(doc)} className="text-gray-500 hover:text-blue-600">
                      <Download className="h-4 w-4" />
                    </button>
                    <button title="Edit" onClick={() => navigate(`/documents/${doc.id}/edit`)} className="text-gray-500 hover:text-blue-600">
                      <Pencil className="h-4 w-4" />
                    </button>
                    {doc.workflowStatus === 'DRAFT' && (
                      <button title="Submit for approval" onClick={() => setConfirmTarget({ type: 'submit', doc })} className="text-gray-500 hover:text-green-600">
                        <Send className="h-4 w-4" />
                      </button>
                    )}
                    {!doc.isArchived ? (
                      <button title="Archive" onClick={() => setConfirmTarget({ type: 'archive', doc })} className="text-gray-500 hover:text-orange-600">
                        <Archive className="h-4 w-4" />
                      </button>
                    ) : (
                      <button title="Restore" onClick={() => setConfirmTarget({ type: 'restore', doc })} className="text-gray-500 hover:text-green-600">
                        <ArchiveRestore className="h-4 w-4" />
                      </button>
                    )}
                    {isAdmin && (
                      <button title="Delete" onClick={() => setConfirmTarget({ type: 'delete', doc })} className="text-gray-500 hover:text-red-600">
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <Pagination page={page} onPageChange={setPageNumber} />
      </div>

      <ConfirmDialog
        open={!!confirmTarget}
        title={confirmTarget ? `${confirmTarget.type[0].toUpperCase()}${confirmTarget.type.slice(1)} document?` : ''}
        message={confirmTarget ? `Are you sure you want to ${confirmTarget.type} "${confirmTarget.doc.title}"?` : ''}
        danger={confirmTarget?.type === 'delete'}
        loading={actionLoading}
        confirmLabel={confirmTarget ? confirmTarget.type[0].toUpperCase() + confirmTarget.type.slice(1) : 'Confirm'}
        onConfirm={runConfirmedAction}
        onCancel={() => setConfirmTarget(null)}
      />
    </div>
  );
}