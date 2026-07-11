package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowSummaryResponse {

    private Long totalInstances;
    private Long pendingInstances;
    private Long inProgressInstances;
    private Long approvedInstances;
    private Long rejectedInstances;
    private Long expiredInstances;

    /** Average time (in hours) between submission and completion, across completed instances. */
    private Double averageCompletionTimeHours;
}