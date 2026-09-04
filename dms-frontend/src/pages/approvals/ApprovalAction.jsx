import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Check, X, RotateCcw } from 'lucide-react';

import Modal  from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import approvalApi from '../../api/approvalApi';

// ─────────────────────────────────────────────────────────────────────────────
// ApprovalAction
// Modal for acting on a pending approval: approve / reject / send-back.
//
// Prop conventions (both supported):
//   • actionType / workflowInstanceId / approvalId / isOpen / title   (spec)
//   • type / item (PendingApprovalResponse)                           (PendingApprovals.jsx)
//
// When only a workflow instance is known, the current (actionable) approval id
// is resolved via approvalApi.getCurrentApproval(instanceId).
// ─────────────────────────────────────────────────────────────────────────────

const ACTION_CONFIG = {
  approve: {
    verb: 'Approve',
    icon: Check,
    apiMethod: 'approveApproval',
    buttonVariant: 'success',
    commentsRequired: false,
    successMsg: 'Approved successfully',
  },
  reject: {
    verb: 'Reject',
    icon: X,
    apiMethod: 'rejectApproval',
    buttonVariant: 'danger',
    commentsRequired: true,
    successMsg: 'Rejected successfully',
  },
  'send-back': {
    verb: 'Send Back',
    icon: RotateCcw,
    apiMethod: 'sendBackApproval',
    buttonVariant: 'primary',
    commentsRequired: true,
    successMsg: 'Document sent back for changes',
  },
};

const ApprovalAction = ({
  isOpen,
  onClose,
  onSuccess,
  approvalId,
  workflowInstanceId,
  actionType,
  type,        // legacy alias for actionType
  item,        // legacy: PendingApprovalResponse
  title,
}) => {
  const action = actionType ?? type ?? 'approve';
  const cfg = ACTION_CONFIG[action] ?? ACTION_CONFIG.approve;
  const Icon = cfg.icon;

  // Legacy callers mount the component conditionally instead of passing isOpen.
  const isVisible = isOpen ?? true;

  const instanceId = workflowInstanceId ?? item?.id ?? null;
  const documentTitle = item?.documentTitle;

  const [comments, setComments]   = useState('');
  const [attachment, setAttachment] = useState(null);
  const [resolvedApprovalId, setResolvedApprovalId] = useState(approvalId ?? null);
  const [resolving, setResolving]   = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Reset form each time the modal opens or the action changes.
  useEffect(() => {
    if (isVisible) {
      setComments('');
      setAttachment(null);
    }
  }, [isVisible, action]);

  // Resolve the current approval id from the workflow instance when not supplied.
  useEffect(() => {
    if (!isVisible) return undefined;

    if (approvalId) {
      setResolvedApprovalId(approvalId);
      return undefined;
    }
    if (!instanceId) return undefined;

    let cancelled = false;
    setResolving(true);
    approvalApi
      .getCurrentApproval(instanceId)
      .then((approval) => {
        if (!cancelled) setResolvedApprovalId(approval?.id ?? null);
      })
      .catch((err) => {
        if (!cancelled) toast.error(err?.message || 'Could not load the approval for this step');
      })
      .finally(() => {
        if (!cancelled) setResolving(false);
      });

    return () => { cancelled = true; };
  }, [isVisible, approvalId, instanceId]);

  const commentsMissing = cfg.commentsRequired && !comments.trim();

  const handleSubmit = async () => {
    if (!resolvedApprovalId) {
      toast.error('No actionable approval was found for this step');
      return;
    }
    if (commentsMissing) {
      toast.error('Comments are required for this action');
      return;
    }

    setSubmitting(true);
    try {
      await approvalApi[cfg.apiMethod](resolvedApprovalId, {
        comments: comments.trim() || undefined,
        attachment: attachment || undefined,
      });
      toast.success(cfg.successMsg);
      onSuccess?.();
    } catch (err) {
      toast.error(err?.message || `Failed to ${cfg.verb.toLowerCase()}`);
    } finally {
      setSubmitting(false);
    }
  };

  const heading = title
    || `${cfg.verb}${documentTitle ? ` — ${documentTitle}` : ''}`;

  return (
    <Modal
      isOpen={isVisible}
      onClose={submitting ? undefined : onClose}
      title={heading}
      size="md"
      closeOnBackdrop={!submitting}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={submitting}>
            Cancel
          </Button>
          <Button
            variant={cfg.buttonVariant}
            onClick={handleSubmit}
            loading={submitting}
            disabled={resolving || !resolvedApprovalId || commentsMissing}
            leftIcon={<Icon size={15} />}
          >
            {cfg.verb}
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        {resolving && (
          <p className="text-xs text-slate-500">Loading approval details…</p>
        )}
        {!resolving && !resolvedApprovalId && (
          <p className="text-sm text-red-600">
            No actionable approval was found for this step. It may have already been actioned.
          </p>
        )}

        <div>
          <label htmlFor="approval-comments" className="block text-sm font-medium text-slate-700 mb-1">
            Comments{' '}
            <span className="text-slate-400 font-normal">
              {cfg.commentsRequired ? '(required)' : '(optional)'}
            </span>
          </label>
          <textarea
            id="approval-comments"
            rows={4}
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            placeholder={
              action === 'approve'
                ? 'Add an optional note for the next approver…'
                : 'Explain why you are ' + (action === 'reject' ? 'rejecting' : 'sending back') + ' this document…'
            }
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700
                       focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
          />
        </div>

        <div>
          <label htmlFor="approval-attachment" className="block text-sm font-medium text-slate-700 mb-1">
            Attachment <span className="text-slate-400 font-normal">(optional)</span>
          </label>
          <input
            id="approval-attachment"
            type="file"
            onChange={(e) => setAttachment(e.target.files?.[0] ?? null)}
            className="block w-full text-sm text-slate-500 file:mr-3 file:rounded-md file:border-0
                       file:bg-slate-100 file:px-3 file:py-1.5 file:text-sm file:font-medium
                       file:text-slate-700 hover:file:bg-slate-200"
          />
        </div>
      </div>
    </Modal>
  );
};

export default ApprovalAction;
