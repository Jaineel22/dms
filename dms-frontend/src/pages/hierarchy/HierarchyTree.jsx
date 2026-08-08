import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ChevronDown, ChevronRight, User as UserIcon } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import hierarchyApi from '../../api/hierarchyApi';

function TreeNode({ node, onSelect, isAdmin }) {
  const [expanded, setExpanded] = useState(true);
  const hasChildren = node.directReports?.length > 0;

  return (
    <div className="ml-2">
      <div className="flex items-center gap-1.5 rounded-md px-2 py-1.5 hover:bg-gray-50">
        {hasChildren ? (
          <button onClick={() => setExpanded((e) => !e)} className="text-gray-400 hover:text-gray-600">
            {expanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
          </button>
        ) : (
          <span className="w-4" />
        )}
        <UserIcon className="h-4 w-4 text-gray-400" />
        <button onClick={() => onSelect(node)} className="text-sm font-medium text-gray-800 hover:text-blue-600">
          {node.fullName || node.name}
        </button>
        <span className="text-xs text-gray-400">L{node.level}</span>
        {isAdmin && (
          <button
            onClick={() => onSelect(node, 'assign')}
            className="ml-2 text-xs text-blue-600 hover:underline"
          >
            Manage
          </button>
        )}
      </div>
      {hasChildren && expanded && (
        <div className="ml-4 border-l border-gray-100 pl-2">
          {node.directReports.map((child) => (
            <TreeNode key={child.userId || child.id} node={child} onSelect={onSelect} isAdmin={isAdmin} />
          ))}
        </div>
      )}
    </div>
  );
}

export default function HierarchyTree() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const navigate = useNavigate();
  const [tree, setTree] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const data = await hierarchyApi.getReportingHierarchy(user?.id);
        setTree(data);
      } catch {
        toast.error('Failed to load hierarchy');
      } finally {
        setLoading(false);
      }
    })();
  }, [user?.id]);

  const handleSelect = (node, action) => {
    if (action === 'assign') {
      navigate(`/hierarchy/assign?userId=${node.userId || node.id}`);
    } else {
      navigate(`/hierarchy/team/${node.userId || node.id}`);
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-900">Organization Hierarchy</h1>
        {isAdmin && (
          <button
            onClick={() => navigate('/hierarchy/assign')}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
          >
            Assign Manager
          </button>
        )}
      </div>
      <div className="rounded-lg border border-gray-200 bg-white p-4">
        {tree ? <TreeNode node={tree} onSelect={handleSelect} isAdmin={isAdmin} /> : (
          <p className="text-sm text-gray-400">No hierarchy data available</p>
        )}
      </div>
    </div>
  );
}