package com.dms.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "workflow_steps",
    uniqueConstraints = @UniqueConstraint(
        name       = "uk_workflow_step",
        columnNames = {"workflow_id", "step_number"}
    )
)
public class WorkflowStep extends BaseEntity {

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Builder.Default
    @Column(name = "is_mandatory")
    private Boolean isMandatory = true;

    @Builder.Default
    @Column(name = "timeout_hours")
    private Integer timeoutHours = 24;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;
}