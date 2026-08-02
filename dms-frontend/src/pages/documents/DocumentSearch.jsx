import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Search, Download } from 'lucide-react';
import documentApi from '../../api/documentApi';
import Pagination from '../../components/common/Pagination';

const emptyFilters = {
  title: '', documentNumber: '', category: '', department: '', status: '', tags: '', dateFrom: '', dateTo: '',
};

export default function DocumentSearch() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState(emptyFilters);
  const [page, setPage] = useState(null);
  const [pageNumber, setPageNumber] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const runSearch = async (pageNum = 0) => {
    setLoading(true);
    setSearched(true);
    try {
      const data = await documentApi.advancedSearch({ ...filters, page: pageNum, size: 20 });
      setPage(data);
      setPageNumber(pageNum);
    } catch (err) {
      toast.error('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    runSearch(0);
  };

  const exportCsv = () => {
    const rows = page?.content || [];
    if (rows.length === 0) {
      toast.error('No results to export');
      return;
    }
    const headers = ['Number', 'Title', 'Category', 'Status', 'Version', 'Uploaded By', 'Created At'];
    const lines = rows.map((d) => [
      d.documentNumber, d.title, d.category, d.workflowStatus, d.currentVersion || 1,
      d.uploadedByName || d.uploadedBy, d.createdAt,
    ].map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','));
    const csv = [headers.join(','), ...lines].join('\n');
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'document-search-results.csv';
    link.click();
    window.URL.revokeObjectURL(url);
  };

  const documents = page?.content || [];

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-gray-900">Advanced Document Search</h1>

      <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4 rounded-lg border border-gray-200 bg-white p-6 md:grid-cols-4">
        {[
          ['title', 'Title'], ['documentNumber', 'Document Number'], ['category', 'Category'],
          ['department', 'Department'], ['tags', 'Tags'],
        ].map(([key, label]) => (
          <div key={key}>
            <label className="mb-1 block text-xs font-medium text-gray-600">{label}</label>
            <input
              value={filters[key]}
              onChange={(e) => setFilters((f) => ({ ...f, [key]: e.target.value }))}
              className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
            />
          </div>
        ))}
        <div>
          <label className="mb-1 block text-xs font-medium text-gray-600">Status</label>
          <select
            value={filters.status}
            onChange={(e) => setFilters((f) => ({ ...f, status: e.target.value }))}
            className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          >
            <option value="">Any</option>
            <option value="DRAFT">Draft</option>
            <option value="UNDER_REVIEW">Under Review</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-gray-600">From Date</label>
          <input type="date" value={filters.dateFrom} onChange={(e) => setFilters((f) => ({ ...f, dateFrom: e.target.value }))} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-gray-600">To Date</label>
          <input type="date" value={filters.dateTo} onChange={(e) => setFilters((f) => ({ ...f, dateTo: e.target.value }))} className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
        </div>
        <div className="col-span-full flex justify-end gap-3">
          <button type="button" onClick={() => setFilters(emptyFilters)} className="rounded-md border border-gray-300 px-4 py-2 text-sm hover:bg-gray-50">
            Clear
          </button>
          <button type="submit" className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
            <Search className="h-4 w-4" /> Search
          </button>
        </div>
      </form>

      {searched && (
        <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
          <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <p className="text-sm text-gray-600">{page?.totalElements ?? 0} result(s)</p>
            <button onClick={exportCsv} className="inline-flex items-center gap-1.5 text-sm text-blue-600 hover:underline">
              <Download className="h-4 w-4" /> Export CSV
            </button>
          </div>
          <table className="min-w-full divide-y divide-gray-200 text-sm">
            <thead className="bg-gray-50">
              <tr>
                {['Number', 'Title', 'Category', 'Status', 'Version', 'Uploaded By', 'Created At'].map((h) => (
                  <th key={h} className="px-4 py-3 text-left font-medium text-gray-500">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading && <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">Searching...</td></tr>}
              {!loading && documents.length === 0 && (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-gray-400">No results</td></tr>
              )}
              {!loading && documents.map((doc) => (
                <tr key={doc.id} className="cursor-pointer hover:bg-gray-50" onClick={() => navigate(`/documents/${doc.id}`)}>
                  <td className="px-4 py-3 font-mono text-xs text-gray-600">{doc.documentNumber}</td>
                  <td className="px-4 py-3 font-medium text-blue-600">{doc.title}</td>
                  <td className="px-4 py-3 text-gray-600">{doc.category}</td>
                  <td className="px-4 py-3 text-gray-600">{doc.workflowStatus}</td>
                  <td className="px-4 py-3 text-gray-600">v{doc.currentVersion || 1}</td>
                  <td className="px-4 py-3 text-gray-600">{doc.uploadedByName || doc.uploadedBy}</td>
                  <td className="px-4 py-3 text-gray-600">{doc.createdAt ? new Date(doc.createdAt).toLocaleDateString() : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination page={page} onPageChange={runSearch} />
        </div>
      )}
    </div>
  );
}