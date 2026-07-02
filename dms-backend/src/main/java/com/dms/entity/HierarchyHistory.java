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
    name    = "hierarchy_history",
    indexes = {
        @Index(name = "idx_hierarchy_user",       columnList = "user_id"),
        @Index(name = "idx_hierarchy_changed_by", columnList = "changed_by")
    }
)
public class HierarchyHistory extends BaseEntity {

    /**
     * The user whose manager assignment changed.
     * Stored as a plain FK value (not a @ManyToOne) to keep audit records
     * intact even if the referenced user is soft-deleted.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Manager before the change — null if the user had no manager.
     */
    @Column(name = "previous_manager_id")
    private Long previousManagerId;

    /**
     * Manager after the change — null if the assignment was cleared.
     */
    @Column(name = "new_manager_id")
    private Long newManagerId;

    /**
     * The admin/system that triggered the manager change.
     */
    @Column(name = "changed_by", nullable = false)
    private Long changedBy;

    /**
     * Optional free-text explanation for the change.
     */
    @Column(name = "change_reason", length = 255)
    private String changeReason;
}