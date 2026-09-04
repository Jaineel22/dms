import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ChevronRight, Users } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import hierarchyApi from '../../api/hierarchyApi';

export default function TeamView() {
  const { managerId: paramManagerId } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const managerId = paramManagerId || user?.id;

  const [directReports, setDirectReports] = useState([]);
  const [chain, setChain] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!managerId) return;
    (async () => {
      setLoading(true);
      try {
        const [reports, reportingChain] = await Promise.all([
          hierarchyApi.getDirectReports(managerId),
          hierarchyApi.getReportingChain(managerId),
        ]);
        setDirectReports(reports || []);
        setChain(reportingChain || []);
      } catch {
        toast.error('Failed to load team');
      } finally {
        setLoading(false);
      }
    })();
  }, [managerId]);

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-1.5 text-sm text-gray-500">
        {chain.map((node, idx) => (
          <span key={node.userId || node.id} className="flex items-center gap-1.5">
            {idx > 0 && <ChevronRight className="h-3.5 w-3.5" />}
            <span>{node.fullName || node.name}</span>
          </span>
        ))}
      </div>

      <h1 className="flex items-center gap-2 text-xl font-semibold text-gray-900">
        <Users className="h-5 w-5" /> Direct Reports
      </h1>

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50">
            <tr>
              {['Name', 'Email', 'Department', 'Hierarchy Level', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-medium text-gray-500">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {directReports.length === 0 && (
              <tr><td colSpan={5} className="px-4 py-8 text-center text-gray-400">No direct reports</td></tr>
            )}
            {directReports.map((report) => (
              <tr key={report.userId || report.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">{report.fullName || report.name}</td>
                <td className="px-4 py-3 text-gray-600">{report.email}</td>
                <td className="px-4 py-3 text-gray-600">{report.department || '-'}</td>
                <td className="px-4 py-3 text-gray-600">L{report.level}</td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => navigate(`/hierarchy/team/${report.userId || report.id}`)}
                    className="text-blue-600 hover:underline"
                  >
                    View their team
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}