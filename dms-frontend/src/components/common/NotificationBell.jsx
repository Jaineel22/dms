import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell } from 'lucide-react';
import toast from 'react-hot-toast';
import notificationApi from '../../api/notificationApi';

export default function NotificationBell() {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [summary, setSummary] = useState(null);
  const containerRef = useRef(null);

  const loadSummary = async () => {
    try {
      const data = await notificationApi.getNotificationSummary();
      setSummary(data);
    } catch {
      // Silently ignore — the bell shouldn't be noisy about polling failures.
    }
  };

  useEffect(() => {
    loadSummary();
    const interval = setInterval(loadSummary, 60000); // poll every minute
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (containerRef.current && !containerRef.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleNotificationClick = async (notification) => {
    try {
      if (!notification.isRead) await notificationApi.markAsRead(notification.id);
      setOpen(false);
      if (notification.link) navigate(notification.link);
      loadSummary();
    } catch {
      toast.error('Failed to mark notification as read');
    }
  };

  const unreadCount = summary?.totalUnreadCount || 0;

  return (
    <div ref={containerRef} className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="relative rounded-full p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-700"
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-[16px] items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-semibold text-white">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-40 mt-2 w-80 rounded-lg border border-gray-200 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-2">
            <p className="text-sm font-semibold text-gray-900">Notifications</p>
            <span className="text-xs text-gray-400">{unreadCount} unread</span>
          </div>
          <ul className="max-h-80 divide-y divide-gray-100 overflow-y-auto">
            {(summary?.recentNotifications || []).length === 0 && (
              <li className="px-4 py-6 text-center text-sm text-gray-400">No recent notifications</li>
            )}
            {(summary?.recentNotifications || []).map((n) => (
              <li key={n.id}>
                <button
                  onClick={() => handleNotificationClick(n)}
                  className={`block w-full px-4 py-3 text-left text-sm hover:bg-gray-50 ${!n.isRead ? 'bg-blue-50/50' : ''}`}
                >
                  <p className="font-medium text-gray-900">{n.title}</p>
                  <p className="mt-0.5 line-clamp-2 text-xs text-gray-500">{n.message}</p>
                  <p className="mt-1 text-[11px] text-gray-400">{n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}</p>
                </button>
              </li>
            ))}
          </ul>
          <button
            onClick={() => { setOpen(false); navigate('/notifications'); }}
            className="block w-full border-t border-gray-100 px-4 py-2 text-center text-sm font-medium text-blue-600 hover:bg-gray-50"
          >
            View all
          </button>
        </div>
      )}
    </div>
  );
}