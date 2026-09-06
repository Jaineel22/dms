-- ============================================================
-- V6.5: Allow approvals.action to be NULL
--
-- WorkflowExecutionServiceImpl.submitDocument() creates the first Approval
-- row for a new workflow instance before anyone has made a decision -- there
-- is no "action" yet, only an approver waiting to act (see isCurrent). The
-- column was NOT NULL, so that very first insert on every submission always
-- failed. action is set once a real decision is made
-- (ApprovalServiceImpl.closeOutApproval -> APPROVED / REJECTED / SENT_BACK).
--
-- The existing chk_ap_action CHECK constraint (action IN ('APPROVED',
-- 'REJECTED','SENT_BACK')) needs no change: a CHECK constraint is satisfied
-- whenever the expression evaluates to NULL, so it already permits NULL
-- values for a pending approval.
-- ============================================================

ALTER TABLE approvals MODIFY COLUMN action VARCHAR(20) NULL;
