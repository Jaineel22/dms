import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { ArrowLeft, Download } from 'lucide-react';
import documentApi from '../../api/documentApi';

export default function DocumentVersionHistory() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [doc, setDoc] = useState(null);
  const [versions, setVersions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [docData, versionData] = await Promise.all([
          documentApi.getDocumentById(id),
          documentApi.getDocumentVersions(id),
        ]);
        setDoc(docData);
        setVersions(versionData || []);
      } catch {
        toast.error('Failed to load version history');
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  const handleDownload = async (versionNumber) => {
    try {
      const blob = await documentApi.downloadDocumentVersion(id, versionNumber);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${doc?.title || 'document'}-v${versionNumber}`;
      link.click();
      window.URL.revokeObjectURL(url);
    } catch {
      toast.error('Download failed');
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400">Loading...</div>;

  return (
    <div className="space-y-6">
      <button onClick={() => navigate(`/documents/${id}`)} className="inline-flex items-center gap-1.5 text-sm text-gray-600 hover:text-gray-900">
        <ArrowLeft className="h-4 w-4" /> Back to document
      </button>

      <h1 className="text-xl font-semibold text-gray-900">Version History — {doc?.title}</h1>

      <div className="overflow-x-auto rounded-lg border border-gray-200 bg-white">
        <table className="min-w-full divide-y divide-gray-200 text-sm">
          <thead className="bg-gray-50">
            <tr>
              {['Version', 'Uploaded By', 'Uploaded At', 'Size', 'Reason', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 text-left font-medium text-gray-500">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {versions.length === 0 && (
              <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400">No versions found</td></tr>
            )}
            {versions.map((v) => (
              <tr key={v.id || v.versionNumber} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium text-gray-900">v{v.versionNumber}</td>
                <td className="px-4 py-3 text-gray-600">{v.uploadedByName || v.uploadedBy}</td>
                <td className="px-4 py-3 text-gray-600">{v.createdAt ? new Date(v.createdAt).toLocaleString() : '-'}</td>
                <td className="px-4 py-3 text-gray-600">{v.fileSize ? `${(v.fileSize / 1024).toFixed(1)} KB` : '-'}</td>
                <td className="px-4 py-3 text-gray-600">{v.uploadReason || '-'}</td>
                <td className="px-4 py-3">
                  <button onClick={() => handleDownload(v.versionNumber)} className="inline-flex items-center gap-1.5 text-blue-600 hover:underline">
                    <Download className="h-4 w-4" /> Download
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <p className="text-xs text-gray-400">Version comparison is planned for a future release.</p>
    </div>
  );
}