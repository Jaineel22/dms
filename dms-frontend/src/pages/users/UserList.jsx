import React, { useState, useEffect, useCallback, useRef } from 'react';
import { UserPlus, Pencil, Trash2, ToggleLeft, ToggleRight, KeyRound } from 'lucide-react';
import toast from 'react-hot-toast';

import PageHeader   from '../../components/common/PageHeader';
import Button       from '../../components/ui/Button';
import Table        from '../../components/ui/Table';
import Pagination   from '../../components/ui/Pagination';
import Card         from '../../components/ui/Card';
import UserFilters  from './UserFilters';
import UserForm     from './UserForm';
import userApi      from '../../api/userApi';
import axiosInstance from '../../api/axios';
import { getStatusColor, getRoleColor, getInitials, getAvatarBg, debounce } from '../../utils/helpers';
import { formatDate, formatRole, formatEmployeeLevel } from '../../utils/formatters';
import { DEFAULT_SIZE } from '../../utils/constants';

const ROLES_ENDPOINT = '/roles'; // adjust if your backend exposes a roles list

const UserList = () => {
  const [users,      setUsers]      = useState([]);
  const [page,       setPage]       = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElems, setTotalElems] = useState(0);
  const [search,     setSearch]     = useState('');
  const [loading,    setLoading]    = useState(true);
  const [sortKey,    setSortKey]    = useState('id');
  const [sortDir,    setSortDir]    = useState('asc');
  const [roles,      setRoles]      = useState([]);

  // Form / delete state
  const [formOpen,    setFormOpen]    = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [deletingId,  setDeletingId]  = useState(null);
  const [actionLoading, setActionLoading] = useState(null); // userId of in-flight action

  // Debounced search ref
  const debouncedSearch = useRef(debounce((val) => { setSearch(val); setPage(0); }, 400)).current;

  // Fetch roles once
  useEffect(() => {
    axiosInstance.get(ROLES_ENDPOINT)
      .then((r) => setRoles(r.data?.data ?? []))
      .catch(() => {});
  }, []);

  const loadUsers = useCallback(async () => {
    setLoading(true);
    try {
      const sort = `${sortKey},${sortDir}`;
      const data = await userApi.getAllUsers({ page, size: DEFAULT_SIZE, sort, search });
      setUsers(data?.content ?? []);
      setTotalPages(data?.totalPages ?? 0);
      setTotalElems(data?.totalElements ?? 0);
    } catch (err) {
      toast.error(err?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }, [page, search, sortKey, sortDir]);

  useEffect(() => { loadUsers(); }, [loadUsers]);

  // ── Sort ──
  const handleSort = (key) => {
    if (key === sortKey) setSortDir((d) => d === 'asc' ? 'desc' : 'asc');
    else { setSortKey(key); setSortDir('asc'); }
    setPage(0);
  };

  // ── Delete ──
  const handleDeleteConfirm = async () => {
    if (!deletingId) return;
    setActionLoading(deletingId);
    try {
      await userApi.deleteUser(deletingId);
      toast.success('User deactivated successfully');
      loadUsers();
    } catch (err) {
      toast.error(err?.message || 'Delete failed');
    } finally {
      setDeletingId(null);
      setActionLoading(null);
    }
  };

  // ── Toggle status ──
  const handleToggle = async (userId) => {
    setActionLoading(userId);
    try {
      await userApi.toggleUserStatus(userId);
      toast.success('User status updated');
      loadUsers();
    } catch (err) {
      toast.error(err?.message || 'Toggle failed');
    } finally {
      setActionLoading(null);
    }
  };

  // ── Reset password ──
  const handleResetPassword = async (userId) => {
    if (!window.confirm('Reset this user\'s password? A temporary password will be generated.')) return;
    setActionLoading(userId);
    try {
      const result = await userApi.resetPassword(userId);
      toast.success(
        `Password reset. Temp password: ${result?.temporaryPassword}`,
        { duration: 10000 }
      );
    } catch (err) {
      toast.error(err?.message || 'Reset failed');
    } finally {
      setActionLoading(null);
    }
  };

  // ── Table columns ──
  const columns = [
    {
      key: 'firstName', label: 'User', sortable: true,
      render: (_, row) => (
        <div className="flex items-center gap-3">
          <div className={`w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-semibold shrink-0 ${getAvatarBg(row.firstName)}`}>
            {getInitials(row.firstName, row.lastName)}
          </div>
          <div className="min-w-0">
            <p className="font-medium text-slate-800 truncate">{row.fullName || `${row.firstName} ${row.lastName}`}</p>
            <p className="text-xs text-slate-400 truncate">{row.email}</p>
          </div>
        </div>
      ),
    },
    {
      key: 'employeeId', label: 'Employee ID', sortable: true,
      render: (v) => <span className="font-mono text-xs bg-slate-100 px-1.5 py-0.5 rounded">{v}</span>,
    },
    {
      key: 'department', label: 'Department',
      render: (_, row) => row.department?.name ?? '—',
    },
    {
      key: 'role', label: 'Role',
      render: (_, row) => {
        const { bg, text } = getRoleColor(row.role?.name);
        return (
          <span className={`badge ${bg} ${text}`}>{formatRole(row.role?.name)}</span>
        );
      },
    },
    {
      key: 'isActive', label: 'Status', sortable: true,
      render: (v) => {
        const { bg, text, dot, label } = getStatusColor(v);
        return (
          <span className={`badge ${bg} ${text} gap-1.5`}>
            <span className={`w-1.5 h-1.5 rounded-full ${dot}`} />
            {label}
          </span>
        );
      },
    },
    {
      key: 'lastLoginAt', label: 'Last Login',
      render: (v) => <span className="text-xs text-slate-500">{formatDate(v)}</span>,
    },
    {
      key: 'actions', label: 'Actions', className: 'text-right',
      render: (_, row) => (
        <div className="flex items-center justify-end gap-1">
          <button
            onClick={() => { setEditingUser(row); setFormOpen(true); }}
            className="p-1.5 rounded-lg text-slate-400 hover:text-primary-600 hover:bg-primary-50 transition-colors"
            title="Edit user"
          >
            <Pencil size={14} />
          </button>
          <button
            onClick={() => handleToggle(row.id)}
            disabled={actionLoading === row.id}
            className="p-1.5 rounded-lg text-slate-400 hover:text-amber-600 hover:bg-amber-50 transition-colors disabled:opacity-40"
            title={row.isActive ? 'Deactivate' : 'Activate'}
          >
            {row.isActive ? <ToggleRight size={14} /> : <ToggleLeft size={14} />}
          </button>
          <button
            onClick={() => handleResetPassword(row.id)}
            disabled={actionLoading === row.id}
            className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors disabled:opacity-40"
            title="Reset password"
          >
            <KeyRound size={14} />
          </button>
          <button
            onClick={() => setDeletingId(row.id)}
            disabled={actionLoading === row.id}
            className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors disabled:opacity-40"
            title="Delete user"
          >
            <Trash2 size={14} />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="User Management"
        subtitle="Manage system users, roles, and access"
        actions={
          <Button
            leftIcon={<UserPlus size={15} />}
            onClick={() => { setEditingUser(null); setFormOpen(true); }}
          >
            Add User
          </Button>
        }
      />

      <Card>
        <Card.Header
          title={`${totalElems} Users`}
          action={
            <UserFilters
              search={search}
              onSearchChange={debouncedSearch}
              onClear={() => { setSearch(''); setPage(0); }}
            />
          }
        />

        <Table
          columns={columns}
          data={users}
          loading={loading}
          sortKey={sortKey}
          sortDir={sortDir}
          onSort={handleSort}
          emptyText="No users found. Try adjusting your search."
        />

        <Pagination
          page={page}
          totalPages={totalPages}
          totalElements={totalElems}
          size={DEFAULT_SIZE}
          onPageChange={setPage}
        />
      </Card>

      {/* Create / Edit modal */}
      <UserForm
        isOpen={formOpen}
        onClose={() => { setFormOpen(false); setEditingUser(null); }}
        user={editingUser}
        roles={roles}
        onSuccess={loadUsers}
      />

      {/* Delete confirmation */}
      {deletingId && (
        <div className="fixed inset-0 z-[1060] flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/50 backdrop-blur-sm" onClick={() => setDeletingId(null)} />
          <div className="relative bg-white rounded-xl shadow-modal p-6 max-w-sm w-full animate-slide-in">
            <h3 className="text-lg font-semibold text-slate-800">Deactivate User?</h3>
            <p className="text-sm text-slate-600 mt-2">
              This will deactivate the user's account. They will no longer be able to log in.
              This action can be reversed by an administrator.
            </p>
            <div className="flex justify-end gap-3 mt-6">
              <Button variant="secondary" onClick={() => setDeletingId(null)}>Cancel</Button>
              <Button variant="danger" onClick={handleDeleteConfirm} loading={actionLoading === deletingId}>
                Deactivate
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserList;