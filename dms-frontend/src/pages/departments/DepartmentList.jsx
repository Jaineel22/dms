import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Plus, Pencil, Trash2, Users, Search, X } from 'lucide-react';
import toast from 'react-hot-toast';

import PageHeader      from '../../components/common/PageHeader';
import Button          from '../../components/ui/Button';
import Card            from '../../components/ui/Card';
import Table           from '../../components/ui/Table';
import Pagination      from '../../components/ui/Pagination';
import Alert           from '../../components/ui/Alert';
import DepartmentForm  from './DepartmentForm';
import departmentApi   from '../../api/departmentApi';
import useAuth         from '../../hooks/useAuth';
import { debounce }    from '../../utils/helpers';
import { formatNumber } from '../../utils/formatters';
import { DEFAULT_SIZE } from '../../utils/constants';

const DepartmentList = () => {
  const { isAdmin } = useAuth();

  /* ── Data state ── */
  const [departments, setDepartments] = useState([]);
  const [page,        setPage]        = useState(0);
  const [totalPages,  setTotalPages]  = useState(0);
  const [totalElems,  setTotalElems]  = useState(0);
  const [loading,     setLoading]     = useState(true);
  const [sortKey,     setSortKey]     = useState('name');
  const [sortDir,     setSortDir]     = useState('asc');
  const [search,      setSearch]      = useState('');

  /* ── UI state ── */
  const [formOpen,    setFormOpen]    = useState(false);
  const [editingDept, setEditingDept] = useState(null);
  const [deletingId,  setDeletingId]  = useState(null);
  const [actionLoading, setActionLoading] = useState(null);
  const [deptUsers,   setDeptUsers]   = useState([]);
  const [viewingDeptId, setViewingDeptId] = useState(null);

  /* Debounced search */
  const debouncedSearch = useRef(
    debounce((val) => { setSearch(val); setPage(0); }, 400)
  ).current;

  /* ── Load departments ── */
  const loadDepartments = useCallback(async () => {
    setLoading(true);
    try {
      const sort = `${sortKey},${sortDir}`;
      const data = await departmentApi.getAllDepartments({
        page, size: DEFAULT_SIZE, sort,
      });
      setDepartments(data?.content ?? []);
      setTotalPages(data?.totalPages ?? 0);
      setTotalElems(data?.totalElements ?? 0);
    } catch (err) {
      toast.error(err?.message || 'Failed to load departments');
    } finally {
      setLoading(false);
    }
  }, [page, sortKey, sortDir]);

  useEffect(() => { loadDepartments(); }, [loadDepartments]);

  /* ── Client-side search filter (backend doesn't expose dept search) ── */
  const filtered = search.trim()
    ? departments.filter((d) =>
        d.name.toLowerCase().includes(search.toLowerCase()) ||
        d.code.toLowerCase().includes(search.toLowerCase())
      )
    : departments;

  /* ── Sort ── */
  const handleSort = (key) => {
    if (key === sortKey) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    else { setSortKey(key); setSortDir('asc'); }
    setPage(0);
  };

  /* ── Delete ── */
  const confirmDelete = async () => {
    if (!deletingId) return;
    setActionLoading(deletingId);
    try {
      await departmentApi.deleteDepartment(deletingId);
      toast.success('Department deleted successfully');
      setDeletingId(null);
      loadDepartments();
    } catch (err) {
      toast.error(err?.message || 'Delete failed. Department may still have users assigned.');
      setDeletingId(null);
    } finally {
      setActionLoading(null);
    }
  };

  /* ── View users ── */
  const handleViewUsers = async (deptId) => {
    try {
      const users = await departmentApi.getDepartmentUsers(deptId);
      setDeptUsers(users ?? []);
      setViewingDeptId(deptId);
    } catch (err) {
      toast.error(err?.message || 'Failed to load department users');
    }
  };

  /* ── Table columns ── */
  const columns = [
    {
      key: 'name', label: 'Department', sortable: true,
      render: (v, row) => (
        <div>
          <p className="font-medium text-slate-800">{v}</p>
          <p className="text-xs text-slate-400 mt-0.5">{row.description || 'No description'}</p>
        </div>
      ),
    },
    {
      key: 'code', label: 'Code', sortable: true, width: '120px',
      render: (v) => (
        <span className="font-mono text-xs font-semibold bg-primary-50 text-primary-700 px-2 py-1 rounded">
          {v}
        </span>
      ),
    },
    {
      key: 'userCount', label: 'Users', width: '100px',
      render: (v, row) => (
        <button
          onClick={() => isAdmin && handleViewUsers(row.id)}
          className={`flex items-center gap-1.5 text-sm font-medium ${
            isAdmin
              ? 'text-primary-600 hover:text-primary-700 hover:underline'
              : 'text-slate-600 cursor-default'
          }`}
          disabled={!isAdmin}
          title={isAdmin ? 'View users in this department' : undefined}
        >
          <Users size={14} />
          {formatNumber(v ?? 0)}
        </button>
      ),
    },
    {
      key: 'isActive', label: 'Status', width: '100px',
      render: (v) => (
        <span className={`badge ${v ? 'badge-success' : 'badge-danger'}`}>
          {v ? 'Active' : 'Inactive'}
        </span>
      ),
    },
    /* Only show actions column to admins */
    ...(isAdmin ? [{
      key: 'actions', label: '', width: '100px',
      render: (_, row) => (
        <div className="flex items-center justify-end gap-1">
          <button
            onClick={() => { setEditingDept(row); setFormOpen(true); }}
            className="p-1.5 rounded-lg text-slate-400 hover:text-primary-600 hover:bg-primary-50 transition-colors"
            title="Edit department"
          >
            <Pencil size={14} />
          </button>
          <button
            onClick={() => setDeletingId(row.id)}
            disabled={actionLoading === row.id}
            className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors disabled:opacity-40"
            title="Delete department"
          >
            <Trash2 size={14} />
          </button>
        </div>
      ),
    }] : []),
  ];

  /* ── Dept users modal column ── */
  const userColumns = [
    {
      key: 'fullName', label: 'Name',
      render: (_, row) => `${row.firstName} ${row.lastName}`,
    },
    { key: 'email',      label: 'Email' },
    { key: 'employeeId', label: 'Employee ID' },
    {
      key: 'isActive', label: 'Status',
      render: (v) => (
        <span className={`badge ${v ? 'badge-success' : 'badge-danger'}`}>
          {v ? 'Active' : 'Inactive'}
        </span>
      ),
    },
  ];

  return (
    <div>
      <PageHeader
        title="Department Management"
        subtitle="Manage organisational departments and their members"
        actions={
          isAdmin && (
            <Button
              leftIcon={<Plus size={15} />}
              onClick={() => { setEditingDept(null); setFormOpen(true); }}
            >
              Add Department
            </Button>
          )
        }
      />

      {!isAdmin && (
        <Alert variant="info" className="mb-4">
          You are viewing departments in read-only mode. Contact an administrator to make changes.
        </Alert>
      )}

      <Card>
        {/* Header with search */}
        <Card.Header
          title={`${totalElems} Department${totalElems !== 1 ? 's' : ''}`}
          action={
            <div className="relative w-64">
              <Search
                size={14}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none"
              />
              <input
                type="search"
                placeholder="Search name or code…"
                onChange={(e) => debouncedSearch(e.target.value)}
                className="form-input pl-9 pr-9 text-sm"
              />
              {search && (
                <button
                  onClick={() => { setSearch(''); }}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                >
                  <X size={13} />
                </button>
              )}
            </div>
          }
        />

        <Table
          columns={columns}
          data={filtered}
          loading={loading}
          sortKey={sortKey}
          sortDir={sortDir}
          onSort={handleSort}
          emptyText={
            search
              ? `No departments match "${search}"`
              : 'No departments found. Create your first department.'
          }
        />

        <Pagination
          page={page}
          totalPages={totalPages}
          totalElements={totalElems}
          size={DEFAULT_SIZE}
          onPageChange={setPage}
        />
      </Card>

      {/* ── Create / Edit modal ── */}
      <DepartmentForm
        isOpen={formOpen}
        onClose={() => { setFormOpen(false); setEditingDept(null); }}
        department={editingDept}
        onSuccess={loadDepartments}
      />

      {/* ── Delete confirmation ── */}
      {deletingId && (
        <div className="fixed inset-0 z-[1060] flex items-center justify-center p-4">
          <div
            className="absolute inset-0 bg-slate-900/50 backdrop-blur-sm"
            onClick={() => setDeletingId(null)}
          />
          <div className="relative bg-white rounded-xl shadow-modal p-6 max-w-sm w-full animate-slide-in">
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center shrink-0">
                <Trash2 size={18} className="text-red-600" />
              </div>
              <div>
                <h3 className="text-base font-semibold text-slate-800">Delete Department?</h3>
                <p className="text-sm text-slate-600 mt-1">
                  This will permanently delete the department. It cannot be deleted if users are
                  still assigned to it.
                </p>
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <Button
                variant="secondary"
                onClick={() => setDeletingId(null)}
                disabled={actionLoading === deletingId}
              >
                Cancel
              </Button>
              <Button
                variant="danger"
                onClick={confirmDelete}
                loading={actionLoading === deletingId}
              >
                Delete
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* ── Department users modal ── */}
      {viewingDeptId && (
        <div className="fixed inset-0 z-[1060] flex items-center justify-center p-4">
          <div
            className="absolute inset-0 bg-slate-900/50 backdrop-blur-sm"
            onClick={() => { setViewingDeptId(null); setDeptUsers([]); }}
          />
          <div className="relative bg-white rounded-xl shadow-modal w-full max-w-2xl max-h-[80vh] flex flex-col animate-slide-in">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 shrink-0">
              <h3 className="font-semibold text-slate-800">
                Department Members
                <span className="ml-2 text-sm font-normal text-slate-400">
                  ({deptUsers.length} user{deptUsers.length !== 1 ? 's' : ''})
                </span>
              </h3>
              <button
                onClick={() => { setViewingDeptId(null); setDeptUsers([]); }}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
              >
                <X size={16} />
              </button>
            </div>
            <div className="flex-1 overflow-y-auto">
              <Table
                columns={userColumns}
                data={deptUsers}
                emptyText="No users assigned to this department."
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DepartmentList;