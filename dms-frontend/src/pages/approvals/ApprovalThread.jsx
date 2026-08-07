import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { ArrowLeft, MessageSquare, Lock, Reply } from 'lucide-react';
import approvalApi from '../../api/approvalApi';

function CommentItem({ comment, onReply }) {
  const [showReply, setShowReply] = useState(false);
  return (
    <div className="rounded-md border border-gray-200 p-4">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-gray-900">
          {comment.userName}
          {comment.isInternal && (
            <span className="ml-2 inline-flex items-center gap-1 rounded-full bg-gray-100 px-2 py-0.5 text-xs text-gray-500">
              <Lock className="h-3 w-3" /> Internal
            </span>
          )}
        </p>
        <span className="text-xs text-gray-400">{comment.createdAt ? new Date(comment.createdAt).toLocaleString() : ''}</span>
      </div>
      <p className="mt-1 text-sm text-gray-700">{comment.content}</p>
      <button onClick={() => setShowReply((s) => !s)} className="mt-2 inline-flex items-center gap-1 text-xs text-blue-600 hover:underline">
        <Reply className="h-3 w-3" /> Reply
      </button>
      {showReply && (
        <ReplyForm parentId={comment.id} onSubmitted={(text, internal) => { onReply(comment.id, text, internal); setShowReply(false); }} />
      )}
      {comment.replies?.length > 0 && (
        <div className="mt-3 space-y-3 border-l-2 border-gray-100 pl-4">
          {comment.replies.map((r) => (
            <CommentItem key={r.id} comment={r} onReply={onReply} />
          ))}
        </div>
      )}
    </div>
  );
}

function ReplyForm({ onSubmitted }) {
  const { register, handleSubmit, reset } = useForm();
  return (
    <form
      onSubmit={handleSubmit((values) => { onSubmitted(values.content, values.isInternal); reset(); })}
      className="mt-2 space-y-2"
    >
      <textarea {...register('content', { required: true })} rows={2} placeholder="Write a reply..." className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
      <div className="flex items-center justify-between">
        <label className="flex items-center gap-1.5 text-xs text-gray-600">
          <input type="checkbox" {...register('isInternal')} className="h-3.5 w-3.5" /> Internal only
        </label>
        <button type="submit" className="rounded-md bg-blue-600 px-3 py-1 text-xs font-medium text-white hover:bg-blue-700">
          Post Reply
        </button>
      </div>
    </form>
  );
}

export default function ApprovalThread() {
  const { documentId } = useParams();
  const navigate = useNavigate();
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);

  const { register, handleSubmit, reset } = useForm();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await approvalApi.getDocumentComments(documentId, 0, 50);
      setComments(data?.content || data || []);
    } catch {
      toast.error('Failed to load comments');
    } finally {
      setLoading(false);
    }
  }, [documentId]);

  useEffect(() => { load(); }, [load]);

  const postComment = async (values) => {
    try {
      await approvalApi.addComment({
        documentId: Number(documentId),
        content: values.content,
        isInternal: !!values.isInternal,
      });
      reset();
      load();
    } catch {
      toast.error('Failed to post comment');
    }
  };

  const postReply = async (parentCommentId, content, isInternal) => {
    try {
      await approvalApi.addComment({ documentId: Number(documentId), parentCommentId, content, isInternal: !!isInternal });
      load();
    } catch {
      toast.error('Failed to post reply');
    }
  };

  return (
    <div className="space-y-6">
      <button onClick={() => navigate(`/documents/${documentId}`)} className="inline-flex items-center gap-1.5 text-sm text-gray-600 hover:text-gray-900">
        <ArrowLeft className="h-4 w-4" /> Back to document
      </button>

      <h1 className="flex items-center gap-2 text-xl font-semibold text-gray-900">
        <MessageSquare className="h-5 w-5" /> Comment Thread
      </h1>

      <form onSubmit={handleSubmit(postComment)} className="space-y-3 rounded-lg border border-gray-200 bg-white p-4">
        <textarea {...register('content', { required: true })} rows={3} placeholder="Add a comment..." className="w-full rounded-md border border-gray-300 px-3 py-2 text-sm" />
        <div className="flex items-center justify-between">
          <label className="flex items-center gap-1.5 text-sm text-gray-600">
            <input type="checkbox" {...register('isInternal')} className="h-4 w-4" /> Internal only (not visible to submitter)
          </label>
          <button type="submit" className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700">
            Post Comment
          </button>
        </div>
      </form>

      <div className="space-y-4">
        {loading && <p className="text-sm text-gray-400">Loading...</p>}
        {!loading && comments.length === 0 && <p className="text-sm text-gray-400">No comments yet.</p>}
        {!loading && comments.map((c) => <CommentItem key={c.id} comment={c} onReply={postReply} />)}
      </div>
    </div>
  );
}