package com.dms.repository;

import com.dms.entity.WorkflowDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Optional<WorkflowDefinition> findByName(String name);

    // ─── Active list queries ─────────────────────────────────────────────────

    List<WorkflowDefinition> findAllByIsActiveTrue();

    Page<WorkflowDefinition> findAllByIsActiveTrue(Pageable pageable);

    List<WorkflowDefinition> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    // ─── Existence checks ────────────────────────────────────────────────────

    Boolean existsByName(String name);

    /** Used during update to exclude the record being updated. */
    Boolean existsByNameAndIdNot(String name, Long id);

    Boolean existsByDepartmentIdAndIsActiveTrue(Long departmentId);

    // ─── Custom queries ───────────────────────────────────────────────────────

    /**
     * Fetch workflow definitions with their steps eagerly to avoid N+1.
     * Only returns active workflows.
     */
    @Query("""
            SELECT DISTINCT w FROM WorkflowDefinition w
            LEFT JOIN FETCH w.steps s
            WHERE w.isActive = true
            ORDER BY w.name ASC
            """)
    List<WorkflowDefinition> findAllActiveWithSteps();

    /**
     * Fetch a single workflow with its steps eagerly.
     */
    @Query("""
            SELECT w FROM WorkflowDefinition w
            LEFT JOIN FETCH w.steps s
            WHERE w.id = :id
            """)
    Optional<WorkflowDefinition> findByIdWithSteps(@Param("id") Long id);

    /**
     * Count workflows that are globally applicable (no department restriction).
     */
    @Query("SELECT COUNT(w) FROM WorkflowDefinition w WHERE w.department IS NULL AND w.isActive = true")
    Long countGlobalActiveWorkflows();
}