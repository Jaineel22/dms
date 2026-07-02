package com.dms.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepRequest {

    @NotNull(message = "Step number is required")
    @Min(value = 1, message = "Step number must be at least 1")
    private Integer stepNumber;

    @NotNull(message = "Approval level is required")
    @Min(value = 1, message = "Approval level must be between 1 and 4")
    @Max(value = 4, message = "Approval level must be between 1 and 4")
    private Integer approvalLevel;

    @NotBlank(message = "Role name is required")
    private String roleName;

    @Builder.Default
    private Boolean isMandatory = true;

    @Min(value = 1, message = "Timeout hours must be at least 1")
    @Builder.Default
    private Integer timeoutHours = 24;
}