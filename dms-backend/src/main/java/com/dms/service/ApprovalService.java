package com.dms.service;

import com.dms.dto.request.ApproveRequest;
import com.dms.dto.request.RejectRequest;
import com.dms.dto.request.SendBackRequest;
import com.dms.dto.response.ApprovalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApprovalService {

    ApprovalResponse approve(ApproveRequest request);

    ApprovalResponse reject(RejectRequest request);

    ApprovalResponse sendBack(SendBackRequest request);

    ApprovalResponse getCurrentApproval(Long instanceId);

    Page<ApprovalResponse> getApprovalHistory(Long instanceId, Pageable pageable);

    void escalateApproval(Long instanceId, String reason, Long toUserId);
}