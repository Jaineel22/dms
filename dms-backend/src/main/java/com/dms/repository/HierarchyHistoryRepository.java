package com.dms.repository;

import com.dms.entity.HierarchyHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HierarchyHistoryRepository extends JpaRepository<HierarchyHistory, Long> {

    // ─── List queries ordered by most recent first ────────────────────────────

    /** Full manager-change history for a specific user. */
    List<HierarchyHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Paginated version of the above — useful for large history sets. */
    Page<HierarchyHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** All manager changes made by a specific admin. */
    List<HierarchyHistory> findByChangedBy(Long changedBy);

    /** All records where a specific user was assigned as the new manager. */
    List<HierarchyHistory> findByUserIdAndNewManagerId(Long userId, Long managerId);

    // ─── Custom queries ───────────────────────────────────────────────────────

    /**
     * Returns the most recent hierarchy change for a user.
     * Useful to confirm the current manager assignment matches the latest record.
     */
    @Query("""
            SELECT h FROM HierarchyHistory h
            WHERE h.userId = :userId
            ORDER BY h.createdAt DESC
            LIMIT 1
            """)
    java.util.Optional<HierarchyHistory> findLatestByUserId(@Param("userId") Long userId);

    /**
     * Returns all history records where the given user previously had
     * a particular manager (useful for reporting org-chart changes).
     */
    @Query("""
            SELECT h FROM HierarchyHistory h
            WHERE h.userId = :userId
              AND (h.previousManagerId = :managerId OR h.newManagerId = :managerId)
            ORDER BY h.createdAt DESC
            """)
    List<HierarchyHistory> findAllByUserIdAndManagerInvolved(
            @Param("userId")    Long userId,
            @Param("managerId") Long managerId);

    /**
     * Count how many manager changes a specific user has had.
     */
    long countByUserId(Long userId);
}