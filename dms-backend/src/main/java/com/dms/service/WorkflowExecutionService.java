package com.dms.service;

import com.dms.dto.request.SubmitDocumentRequest;
import com.dms.dto.response.ApprovalResponse;
import com.dms.dto.response.PendingApprovalResponse;
import com.dms.dto.response.WorkflowInstanceResponse;
import com.dms.dto.response.WorkflowSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkflowExecutionService {

    WorkflowInstanceResponse submitDocument(SubmitDocumentRequest request);

    WorkflowInstanceResponse getWorkflowInstance(Long instanceId);

    Page<PendingApprovalResponse> getPendingApprovals(Long userId, Pageable pageable);

    List<ApprovalResponse> getApprovalHistory(Long documentId);

    Page<WorkflowInstanceResponse> getInstancesByUser(Long userId, Pageable pageable);

    WorkflowSummaryResponse getWorkflowSummary();

    void cancelWorkflow(Long instanceId);
}