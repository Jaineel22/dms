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
public class PendingApprovalResponse {

    /** Workflow instance id. */
    private Long id;
    private Long documentId;
    private String documentTitle;
    private String documentNumber;
    private Long stepId;
    private String stepName;
    private Integer stepNumber;
    private Long submittedBy;
    private String submittedByName;
    private LocalDateTime submittedAt;
    private LocalDateTime deadlineAt;
    private Long daysRemaining;
}