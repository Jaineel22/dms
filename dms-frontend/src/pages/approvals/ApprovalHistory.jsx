import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ArrowLeft } from 'lucide-react';
import approvalApi from '../../api/approvalApi';

const ACTION_STYLES = {
  APPROVED: 'bg-green-500',
  REJECTED: 'bg-red-500',
  SENT_BACK: 'bg-yellow-500',
};

export default function ApprovalHistory() {
  const { documentId } = useParams();
  const navigate = useNavigate();
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const data = await approvalApi.getApprovalHistory(documentId);
        setHistory(data || []);
      } catch {
        toast.error('Failed to load approval history');
      } finally {
        setLoading(false);
      }
    })();
  }, [documentId]);

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="space-y-6">
      <button onClick={() => navigate(`/documents/${documentId}`)} className="inline-flex items-center gap-1.5 text-sm text-gray-600 hover:text-gray-900">
        <ArrowLeft className="h-4 w-4" /> Back to document
      </button>

      <h1 className="text-xl font-semibold text-gray-900">Approval History</h1>

      <div className="rounded-lg border border-gray-200 bg-white p-6">
        {history.length === 0 ? (
          <p className="text-sm text-gray-400">No approval activity yet for this document.</p>
        ) : (
          <ol className="space-y-6 border-l-2 border-gray-100 pl-6">
            {history.map((h, idx) => (
              <li key={h.id} className="relative">
                <span className={`absolute -left-[29px] top-1 h-3.5 w-3.5 rounded-full ring-4 ring-white ${ACTION_STYLES[h.action] || 'bg-gray-400'}`} />
                <div className={`rounded-md border p-4 ${idx === 0 ? 'border-blue-200 bg-blue-50' : 'border-gray-200'}`}>
                  <div className="flex items-center justify-between">
                    <p className="font-medium text-gray-900">{h.stepName}</p>
                    <span className="text-xs font-medium text-gray-500">{h.action}</span>
                  </div>
                  <p className="mt-1 text-sm text-gray-600">Approver: {h.approverName}</p>
                  <p className="text-xs text-gray-400">{h.performedAt ? new Date(h.performedAt).toLocaleString() : 'Pending'}</p>
                  {h.comments && <p className="mt-2 rounded bg-white/60 p-2 text-sm text-gray-700">"{h.comments}"</p>}
                  {h.attachmentPath && (
                    <a href={h.attachmentPath} target="_blank" rel="noreferrer" className="mt-2 inline-block text-xs text-blue-600 hover:underline">
                      View attachment
                    </a>
                  )}
                </div>
              </li>
            ))}
          </ol>
        )}
      </div>
    </div>
  );
}