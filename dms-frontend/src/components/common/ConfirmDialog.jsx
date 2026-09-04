import { AlertTriangle, AlertCircle, Info } from 'lucide-react';

import Modal  from '../ui/Modal';
import Button from '../ui/Button';

// ─────────────────────────────────────────────────────────────────────────────
// ConfirmDialog
// Reusable confirmation modal built on the shared Modal + Button components.
//
// Supports two prop conventions so it works with every caller in the app:
//   • isOpen / onClose / confirmText / cancelText / variant / isLoading   (spec)
//   • open   / onCancel / confirmLabel / danger / loading                 (legacy)
//
// @param {boolean}  isOpen | open
// @param {Function} onClose | onCancel   - dismiss without confirming
// @param {Function} onConfirm            - user confirmed the action
// @param {string}   title
// @param {string|React.ReactNode} message
// @param {string}   confirmText | confirmLabel   default 'Confirm'
// @param {string}   cancelText                   default 'Cancel'
// @param {'danger'|'warning'|'info'} variant     default 'warning'
// @param {boolean}  danger                       shorthand for variant='danger'
// @param {boolean}  isLoading | loading          spinner + disabled confirm button
// ─────────────────────────────────────────────────────────────────────────────

const VARIANT_CONFIG = {
  danger: {
    icon: AlertTriangle,
    iconWrap: 'bg-red-100 text-red-600',
    button: 'danger',
  },
  warning: {
    icon: AlertCircle,
    iconWrap: 'bg-amber-100 text-amber-600',
    button: 'primary',
  },
  info: {
    icon: Info,
    iconWrap: 'bg-blue-100 text-blue-600',
    button: 'primary',
  },
};

const ConfirmDialog = ({
  isOpen,
  open,
  onClose,
  onCancel,
  onConfirm,
  title = 'Are you sure?',
  message,
  confirmText,
  confirmLabel,
  cancelText = 'Cancel',
  variant,
  danger = false,
  isLoading,
  loading = false,
}) => {
  const isVisible   = isOpen ?? open ?? false;
  const dismiss     = onClose ?? onCancel ?? (() => {});
  const busy        = isLoading ?? loading ?? false;
  const confirmCopy = confirmText ?? confirmLabel ?? 'Confirm';
  const resolvedVariant = variant ?? (danger ? 'danger' : 'warning');
  const cfg = VARIANT_CONFIG[resolvedVariant] ?? VARIANT_CONFIG.warning;
  const Icon = cfg.icon;

  return (
    <Modal
      isOpen={isVisible}
      onClose={busy ? undefined : dismiss}
      title={title}
      size="sm"
      closeOnBackdrop={!busy}
      footer={
        <>
          <Button variant="secondary" onClick={dismiss} disabled={busy}>
            {cancelText}
          </Button>
          <Button variant={cfg.button} onClick={onConfirm} loading={busy}>
            {confirmCopy}
          </Button>
        </>
      }
    >
      <div className="flex gap-4">
        <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${cfg.iconWrap}`}>
          <Icon size={20} />
        </div>
        <div className="text-sm text-slate-600 leading-relaxed pt-1.5">
          {message}
        </div>
      </div>
    </Modal>
  );
};

export default ConfirmDialog;
