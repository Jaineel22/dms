import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Check, CheckCheck, Trash2 } from 'lucide-react';
import notificationApi from '../../api/notificationApi';
import Pagination from '../../components/common/Pagination';

const TYPES = ['APPROVAL_REQUIRED', 'APPROVED', 'REJECTED', 'SENT_BACK', 'ESCALATED', 'COMMENT_ADDED', 'DOCUMENT_UPLOADED', 'DOCUMENT_SUBMITTED', 'REMINDER'];

export default function NotificationList() {
  const navigate = useNavigate();
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(true);
  const [typeFilter, setTypeFilter] = useState('');
  const [readFilter, setReadFilter] = useState('ALL'); // ALL | READ | UNREAD

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = readFilter === 'UNREAD'
        ? await notificationApi.getUnreadNotifications(pageNumber, 20)
        : await notificationApi.getNotifications(pageNumber, 20);
      setPage(data);
    } catch {
      toast.error('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  }, [pageNumber, readFilter]);

  useEffect(() => { load(); }, [load]);

  const markRead = async (id) => {
    try {
      await notificationApi.markAsRead(id);
      load();
    } catch {
      toast.error('Failed to mark as read');
    }
  };

  const markAllRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      toast.success('All notifications marked as read');
      load();
    } catch {
      toast.error('Failed to mark all as read');
    }
  };

  const remove = async (id) => {
    try {
      await notificationApi.deleteNotification(id);
      load();
    } catch {
      toast.error('Failed to delete notification');
    }
  };

  let items = page?.content || [];
  if (typeFilter) items = items.filter((n) => n.type === typeFilter);
  if (readFilter === 'READ') items = items.filter((n) => n.isRead);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-900">Notifications</h1>
        <button onClick={markAllRead} className="inline-flex items-center gap-1.5 rounded-md border border-gray-300 px-3 py-1.5 text-sm hover:bg-gray-50">
          <CheckCheck className="h-4 w-4" /> Mark all as read
        </button>
      </div>

      <div className="flex flex-wrap gap-3">
        <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)} className="rounded-md border border-gray-300 px-3 py-2 text-sm">
          <option value="">All Types</option>
          {TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
        </select>
        <select value={readFilter} onChange={(e) => { setReadFilter(e.target.value); setPageNumber(0); }} className="rounded-md border border-gray-300 px-3 py-2 text-sm">
          <option value="ALL">All</option>
          <option value="UNREAD">Unread</option>
          <option value="READ">Read</option>
        </select>
      </div>

      <div className="overflow-hidden rounded-lg border border-gray-200 bg-white">
        <ul className="divide-y divide-gray-100">
          {loading && <li className="px-4 py-8 text-center text-gray-400">Loading...</li>}
          {!loading && items.length === 0 && <li className="px-4 py-8 text-center text-gray-400">No notifications</li>}
          {!loading && items.map((n) => (
            <li key={n.id} className={`flex items-start justify-between px-4 py-4 ${!n.isRead ? 'bg-blue-50/40' : ''}`}>
              <button className="flex-1 text-left" onClick={() => n.link && navigate(n.link)}>
                <div className="flex items-center gap-2">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${n.priority === 'HIGH' ? 'bg-red-100 text-red-700' : n.priority === 'LOW' ? 'bg-gray-100 text-gray-600' : 'bg-yellow-100 text-yellow-700'}`}>
                    {n.priority}
                  </span>
                  <p className="text-sm font-medium text-gray-900">{n.title}</p>
                </div>
                <p className="mt-1 text-sm text-gray-600">{n.message}</p>
                <p className="mt-1 text-xs text-gray-400">{n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}</p>
              </button>
              <div className="ml-4 flex items-center gap-2">
                {!n.isRead && (
                  <button title="Mark as read" onClick={() => markRead(n.id)} className="text-gray-400 hover:text-green-600">
                    <Check className="h-4 w-4" />
                  </button>
                )}
                <button title="Delete" onClick={() => remove(n.id)} className="text-gray-400 hover:text-red-600">
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </li>
          ))}
        </ul>
        <Pagination
          page={page?.number ?? pageNumber}
          totalPages={page?.totalPages ?? 0}
          totalElements={page?.totalElements ?? 0}
          size={page?.size ?? 20}
          onPageChange={setPageNumber}
        />
      </div>
    </div>
  );
}