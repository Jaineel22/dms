package com.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "workflow_definitions")
public class WorkflowDefinition extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(
        mappedBy = "workflow",
        fetch    = FetchType.LAZY,
        cascade  = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("stepNumber ASC")
    @Builder.Default
    private List<WorkflowStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "workflow", fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserWorkflow> userWorkflows = new ArrayList<>();
}