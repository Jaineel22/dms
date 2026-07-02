package com.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "user_workflows",
    uniqueConstraints = @UniqueConstraint(
        name        = "uk_user_workflow",
        columnNames = {"user_id", "workflow_id"}
    ),
    indexes = {
        @Index(name = "idx_user_workflow_user",   columnList = "user_id"),
        @Index(name = "idx_user_workflow_active",  columnList = "is_active")
    }
)
public class UserWorkflow extends BaseEntity {

    /**
     * The user this workflow assignment belongs to.
     * Stored as a plain FK column (Long) so assignments survive soft-deletes.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt = LocalDateTime.now();

    /**
     * The admin/system user who created this assignment.
     */
    @Column(name = "assigned_by")
    private Long assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private WorkflowDefinition workflow;
}