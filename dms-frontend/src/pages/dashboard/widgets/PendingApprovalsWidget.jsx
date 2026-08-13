import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ClipboardCheck } from 'lucide-react';
import approvalApi from '../../../api/approvalApi';

export default function PendingApprovalsWidget({ limit = 5 }) {
  const navigate = useNavigate();
  const [items, setItems] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const data = await approvalApi.getPendingApprovals(0, limit);
        setItems(data?.content || []);
        setTotal(data?.totalElements ?? (data?.content?.length || 0));
      } catch {
        toast.error('Failed to load pending approvals');
      } finally {
        setLoading(false);
      }
    })();
  }, [limit]);

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-5">
      <div className="mb-3 flex items-center justify-between">
        <h2 className="flex items-center gap-2 text-sm font-semibold text-gray-900">
          <ClipboardCheck className="h-4 w-4" /> Pending Approvals
        </h2>
        <span className="rounded-full bg-yellow-100 px-2 py-0.5 text-xs font-medium text-yellow-700">
          {total}
        </span>
      </div>

      {loading && <p className="text-sm text-gray-400">Loading...</p>}
      {!loading && items.length === 0 && <p className="text-sm text-gray-400">Nothing pending — you're all caught up.</p>}
      {!loading && items.length > 0 && (
        <ul className="divide-y divide-gray-100">
          {items.map((item) => (
            <li key={item.id}>
              <button
                onClick={() => navigate('/approvals/pending')}
                className="flex w-full items-center justify-between py-2.5 text-left text-sm hover:bg-gray-50"
              >
                <span className="truncate">
                  <span className="font-medium text-gray-900">{item.documentTitle}</span>{' '}
                  <span className="text-gray-500">— {item.stepName}</span>
                </span>
                <span className={`ml-3 whitespace-nowrap text-xs font-medium ${
                  item.daysRemaining < 0 ? 'text-red-600' : item.daysRemaining <= 1 ? 'text-orange-600' : 'text-gray-400'
                }`}>
                  {item.deadlineAt ? new Date(item.deadlineAt).toLocaleDateString() : 'No deadline'}
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}

      {!loading && total > items.length && (
        <button onClick={() => navigate('/approvals/pending')} className="mt-3 text-sm font-medium text-blue-600 hover:underline">
          View all {total} pending approvals
        </button>
      )}
    </div>
  );
}