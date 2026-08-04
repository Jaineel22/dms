import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Check, X, RotateCcw } from 'lucide-react';
import approvalApi from '../../api/approvalApi';
import Pagination from '../../components/common/Pagination';
import ApprovalAction from './ApprovalAction';

export default function PendingApprovals() {
  const navigate = useNavigate();
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(true);
  const [actionModal, setActionModal] = useState(null); // { type: 'approve'|'reject'|'send-back', item }
  const [filterDocument, setFilterDocument] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await approvalApi.getPendingApprovals(pageNumber, 20);
      setPage(data);
    } catch {
      toast.error('Failed to load pending approvals');
    } finally {
      setLoading(false);
    }
  }, [pageNumber]);

  useEffect(() => { load(); }, [load]);

  const items = (page?.content || []).filter((item) =>
    !filterDocument || item.documentTitle?.toLowerCase().includes(filterDocument.toLowerCase()));

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-900">Pending Approvals</h1>
        <input
          value={filterDocument}
          onChange={(e) => setFilterDocument(e.target.value)}
          placeholder="Filter by document..."
          className="rounded-md border border-gray-300 px-3 py-2 text-sm"
        />
      </div>

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50">
            <tr>
              {['Document', 'Step', 'Submitted By', 'Submitted At', 'Deadline', 'Days Remaining', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-medium text-gray-500">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {loading && <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">Loading...</td></tr>}
            {!loading && items.length === 0 && (
              <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">No pending approvals</td></tr>
            )}
            {!loading && items.map((item) => (
              <tr key={item.id} className="hover:bg-gray-50">
                <td className="px-4 py-3">
                  <button onClick={() => navigate(`/documents/${item.documentId}`)} className="font-medium text-blue-600 hover:underline">
                    {item.documentTitle}
                  </button>
                  <div className="font-mono text-xs text-gray-400">{item.documentNumber}</div>
                </td>
                <td className="px-4 py-3 text-gray-600">{item.stepName} (#{item.stepNumber})</td>
                <td className="px-4 py-3 text-gray-600">{item.submittedByName}</td>
                <td className="px-4 py-3 text-gray-600">{item.submittedAt ? new Date(item.submittedAt).toLocaleDateString() : '-'}</td>
                <td className="px-4 py-3 text-gray-600">{item.deadlineAt ? new Date(item.deadlineAt).toLocaleDateString() : '-'}</td>
                <td className="px-4 py-3">
                  <span className={`font-medium ${item.daysRemaining < 0 ? 'text-red-600' : item.daysRemaining <= 1 ? 'text-orange-600' : 'text-gray-700'}`}>
                    {item.daysRemaining ?? '-'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <button title="Approve" onClick={() => setActionModal({ type: 'approve', item })} className="rounded-md bg-green-50 p-1.5 text-green-600 hover:bg-green-100">
                      <Check className="h-4 w-4" />
                    </button>
                    <button title="Reject" onClick={() => setActionModal({ type: 'reject', item })} className="rounded-md bg-red-50 p-1.5 text-red-600 hover:bg-red-100">
                      <X className="h-4 w-4" />
                    </button>
                    <button title="Send Back" onClick={() => setActionModal({ type: 'send-back', item })} className="rounded-md bg-yellow-50 p-1.5 text-yellow-600 hover:bg-yellow-100">
                      <RotateCcw className="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <Pagination page={page} onPageChange={setPageNumber} />
      </div>

      {actionModal && (
        <ApprovalAction
          type={actionModal.type}
          item={actionModal.item}
          onClose={() => setActionModal(null)}
          onSuccess={() => { setActionModal(null); load(); }}
        />
      )}
    </div>
  );
}