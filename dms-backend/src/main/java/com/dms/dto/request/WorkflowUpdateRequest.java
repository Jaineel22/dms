package com.dms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowUpdateRequest {

    private String name;
    private String description;
    private Long departmentId;
    private Boolean isActive;
}