package com.dms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCreateRequest {

    @NotBlank(message = "Workflow name is required")
    @Size(max = 100, message = "Workflow name must not exceed 100 characters")
    private String name;

    private String description;

    private Long departmentId;

    @NotNull(message = "Workflow steps are required")
    @Size(min = 2, message = "Workflow must have at least 2 steps")
    @Valid
    private List<WorkflowStepRequest> steps;
}