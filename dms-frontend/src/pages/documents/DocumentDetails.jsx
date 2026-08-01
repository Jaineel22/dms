import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  Download, Pencil, Archive, ArchiveRestore, Trash2, Send, MessageSquare, Clock,
} from 'lucide-react';
import documentApi from '../../api/documentApi';
import approvalApi from '../../api/approvalApi';
import ConfirmDialog from '../../components/common/ConfirmDialog';

export default function DocumentDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [doc, setDoc] = useState(null);
  const [versions, setVersions] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [confirmTarget, setConfirmTarget] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [docData, versionData] = await Promise.all([
        documentApi.getDocumentById(id),
        documentApi.getDocumentVersions(id),
      ]);
      setDoc(docData);
      setVersions(versionData || []);
      try {
        const historyData = await approvalApi.getApprovalHistory(id);
        setHistory(historyData || []);
      } catch {
        setHistory([]); // no workflow instance yet for this document
      }
    } catch (err) {
      toast.error('Failed to load document');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { load(); }, [load]);

  const handleDownload = async () => {
    try {
      const blob = await documentApi.downloadDocument(id);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = doc.fileName || doc.title;
      link.click();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error('Download failed');
    }
  };

  const runConfirmedAction = async () => {
    if (!confirmTarget) return;
    setActionLoading(true);
    try {
      if (confirmTarget === 'archive') {
        await documentApi.archiveDocument(id);
        toast.success('Document archived');
      } else if (confirmTarget === 'restore') {
        await documentApi.restoreDocument(id);
        toast.success('Document restored');
      } else if (confirmTarget === 'delete') {
        await documentApi.deleteDocument(id);
        toast.success('Document deleted');
        navigate('/documents');
        return;
      } else if (confirmTarget === 'submit') {
        await approvalApi.submitDocument({ documentId: Number(id) });
        toast.success('Submitted for approval');
      }
      setConfirmTarget(null);
      load();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Action failed');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading || !doc) {
    return <div className="p-8 text-center text-gray-400">Loading...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-semibold text-gray-900">{doc.title}</h1>
          <p className="mt-1 font-mono text-xs text-gray-500">{doc.documentNumber}</p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={handleDownload} className="inline-flex items-center gap-1.5 rounded-md border border-gray-300 px-3 py-1.5 text-sm hover:bg-gray-50">
            <Download className="h-4 w-4" /> Download
          </button>
          <button onClick={() => navigate(`/documents/${id}/edit`)} className="inline-flex items-center gap-1.5 rounded-md border border-gray-300 px-3 py-1.5 text-sm hover:bg-gray-50">
            <Pencil className="h-4 w-4" /> Edit
          </button>
          {doc.workflowStatus === 'DRAFT' && (
            <button onClick={() => setConfirmTarget('submit')} className="inline-flex items-center gap-1.5 rounded-md bg-blue-600 px-3 py-1.5 text-sm text-white hover:bg-blue-700">
              <Send className="h-4 w-4" /> Submit for Approval
            </button>
          )}
          {!doc.isArchived ? (
            <button onClick={() => setConfirmTarget('archive')} className="inline-flex items-center gap-1.5 rounded-md border border-gray-300 px-3 py-1.5 text-sm hover:bg-gray-50">
              <Archive className="h-4 w-4" /> Archive
            </button>
          ) : (
            <button onClick={() => setConfirmTarget('restore')} className="inline-flex items-center gap-1.5 rounded-md border border-gray-300 px-3 py-1.5 text-sm hover:bg-gray-50">
              <ArchiveRestore className="h-4 w-4" /> Restore
            </button>
          )}
          <button onClick={() => setConfirmTarget('delete')} className="inline-flex items-center gap-1.5 rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50">
            <Trash2 className="h-4 w-4" /> Delete
          </button>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-6">
        <div className="col-span-2 space-y-6">
          <section className="rounded-lg border border-gray-200 bg-white p-5">
            <h2 className="mb-3 text-sm font-semibold text-gray-900">Document Info</h2>
            <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
              <dt className="text-gray-500">Status</dt><dd className="text-gray-900">{doc.workflowStatus}</dd>
              <dt className="text-gray-500">Version</dt><dd className="text-gray-900">v{doc.currentVersion || 1}</dd>
              <dt className="text-gray-500">Category</dt><dd className="text-gray-900">{doc.category}</dd>
              <dt className="text-gray-500">Department</dt><dd className="text-gray-900">{doc.department || '-'}</dd>
              <dt className="text-gray-500">Tags</dt><dd className="text-gray-900">{doc.tags || '-'}</dd>
              <dt className="text-gray-500">File</dt><dd className="text-gray-900">{doc.fileName} ({((doc.fileSize || 0) / 1024).toFixed(1)} KB)</dd>
              <dt className="text-gray-500">Uploaded By</dt><dd className="text-gray-900">{doc.uploadedByName || doc.uploadedBy}</dd>
              <dt className="text-gray-500">Created At</dt><dd className="text-gray-900">{doc.createdAt ? new Date(doc.createdAt).toLocaleString() : '-'}</dd>
            </dl>
          </section>

          <section className="rounded-lg border border-gray-200 bg-white p-5">
            <h2 className="mb-3 text-sm font-semibold text-gray-900">Version History</h2>
            <ul className="divide-y divide-gray-100">
              {versions.length === 0 && <li className="py-3 text-sm text-gray-400">No previous versions</li>}
              {versions.map((v) => (
                <li key={v.id || v.versionNumber} className="flex items-center justify-between py-3 text-sm">
                  <div>
                    <p className="font-medium text-gray-900">Version {v.versionNumber}</p>
                    <p className="text-gray-500">{v.uploadedByName || v.uploadedBy} · {v.createdAt ? new Date(v.createdAt).toLocaleString() : ''}</p>
                    {v.uploadReason && <p className="mt-1 text-gray-400">{v.uploadReason}</p>}
                  </div>
                  <button
                    onClick={async () => {
                      const blob = await documentApi.downloadDocumentVersion(id, v.versionNumber);
                      const url = window.URL.createObjectURL(blob);
                      const link = document.createElement('a');
                      link.href = url;
                      link.download = `${doc.title}-v${v.versionNumber}`;
                      link.click();
                      window.URL.revokeObjectURL(url);
                    }}
                    className="text-blue-600 hover:underline"
                  >
                    Download
                  </button>
                </li>
              ))}
            </ul>
          </section>

          <section className="rounded-lg border border-gray-200 bg-white p-5">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-900">
              <MessageSquare className="h-4 w-4" /> Comments
            </h2>
            <Link to={`/approvals/thread/${id}`} className="text-sm text-blue-600 hover:underline">
              View comment thread
            </Link>
          </section>
        </div>

        <div className="space-y-6">
          <section className="rounded-lg border border-gray-200 bg-white p-5">
            <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold text-gray-900">
              <Clock className="h-4 w-4" /> Approval Timeline
            </h2>
            {history.length === 0 ? (
              <p className="text-sm text-gray-400">Not yet submitted for approval</p>
            ) : (
              <ol className="space-y-4 border-l border-gray-200 pl-4">
                {history.map((h) => (
                  <li key={h.id} className="relative">
                    <span className={`absolute -left-[21px] top-1 h-3 w-3 rounded-full ${
                      h.action === 'APPROVED' ? 'bg-green-500' : h.action === 'REJECTED' ? 'bg-red-500' : 'bg-yellow-500'
                    }`} />
                    <p className="text-sm font-medium text-gray-900">{h.stepName} — {h.action}</p>
                    <p className="text-xs text-gray-500">{h.approverName} · {h.performedAt ? new Date(h.performedAt).toLocaleString() : ''}</p>
                    {h.comments && <p className="mt-1 text-xs text-gray-600">"{h.comments}"</p>}
                  </li>
                ))}
              </ol>
            )}
          </section>
        </div>
      </div>

      <ConfirmDialog
        open={!!confirmTarget}
        title={confirmTarget ? `${confirmTarget[0].toUpperCase()}${confirmTarget.slice(1)} document?` : ''}
        message={confirmTarget ? `Are you sure you want to ${confirmTarget} "${doc.title}"?` : ''}
        danger={confirmTarget === 'delete'}
        loading={actionLoading}
        confirmLabel={confirmTarget ? confirmTarget[0].toUpperCase() + confirmTarget.slice(1) : 'Confirm'}
        onConfirm={runConfirmedAction}
        onCancel={() => setConfirmTarget(null)}
      />
    </div>
  );
}