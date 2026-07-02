package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepResponse {

    private Long id;
    private Integer stepNumber;
    private Integer approvalLevel;
    private String roleName;
    private Boolean isMandatory;
    private Integer timeoutHours;
}