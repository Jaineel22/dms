package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {

    private Long id;
    private Long workflowInstanceId;
    private Long stepId;
    private String stepName;
    private Long approverId;
    private String approverName;
    private String action;
    private String comments;
    private String attachmentPath;
    private LocalDateTime performedAt;
    private Boolean isCurrent;
}