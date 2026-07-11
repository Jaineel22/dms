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
public class WorkflowInstanceResponse {

    private Long id;
    private Long documentId;
    private Long workflowDefinitionId;
    private String workflowName;
    private Long currentStepId;
    private String currentStepName;
    private Long currentApproverId;
    private String currentApproverName;
    private String status;
    private Long submittedBy;
    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;
    private LocalDateTime deadlineAt;
    private Boolean isActive;
    private LocalDateTime createdAt;
}