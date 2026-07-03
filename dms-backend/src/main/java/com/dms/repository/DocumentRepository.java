package com.dms.repository;

import com.dms.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Optional<Document> findByDocumentNumber(String documentNumber);

    // ─── Existence checks ────────────────────────────────────────────────────

    Boolean existsByDocumentNumber(String documentNumber);

    /** Exclude self during update uniqueness check. */
    Boolean existsByDocumentNumberAndIdNot(String documentNumber, Long id);

    // ─── Filtered page queries ────────────────────────────────────────────────

    Page<Document> findByOwnerIdAndIsArchivedFalse(Long ownerId, Pageable pageable);

    Page<Document> findByDepartmentIdAndIsArchivedFalse(Long departmentId, Pageable pageable);

    Page<Document> findByCategoryIdAndIsArchivedFalse(Long categoryId, Pageable pageable);

    Page<Document> findByStatusAndIsArchivedFalse(String status, Pageable pageable);

    Page<Document> findAllByIsArchivedFalse(Pageable pageable);

    // ─── Full-text search ─────────────────────────────────────────────────────

    @Query("""
            SELECT d FROM Document d
            WHERE d.isArchived = false
              AND (
                    LOWER(d.title)          LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(d.documentNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(d.description)   LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(d.tags)          LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """)
    Page<Document> searchDocuments(@Param("search") String search, Pageable pageable);

    // ─── Advanced multi-filter search ─────────────────────────────────────────

    /**
     * Multi-criteria search — all parameters are optional (null = ignored).
     * Callers pass null for any dimension they do not want to filter on.
     */
    @Query("""
            SELECT d FROM Document d
            WHERE d.isArchived = false
              AND (:title          IS NULL OR LOWER(d.title)          LIKE LOWER(CONCAT('%', :title, '%')))
              AND (:documentNumber IS NULL OR LOWER(d.documentNumber) LIKE LOWER(CONCAT('%', :documentNumber, '%')))
              AND (:categoryId     IS NULL OR d.category.id   = :categoryId)
              AND (:departmentId   IS NULL OR d.department.id = :departmentId)
              AND (:status         IS NULL OR d.status        = :status)
              AND (:ownerId        IS NULL OR d.owner.id      = :ownerId)
              AND (:fromDate       IS NULL OR d.createdAt    >= :fromDate)
              AND (:toDate         IS NULL OR d.createdAt    <= :toDate)
            """)
    Page<Document> advancedSearch(
            @Param("title")          String        title,
            @Param("documentNumber") String        documentNumber,
            @Param("categoryId")     Long          categoryId,
            @Param("departmentId")   Long          departmentId,
            @Param("status")         String        status,
            @Param("ownerId")        Long          ownerId,
            @Param("fromDate")       LocalDateTime fromDate,
            @Param("toDate")         LocalDateTime toDate,
            Pageable pageable
    );

    // ─── Count helpers ────────────────────────────────────────────────────────

    Long countByOwnerIdAndIsArchivedFalse(Long ownerId);

    Long countByStatusAndIsArchivedFalse(String status);

    Long countByDepartmentIdAndIsArchivedFalse(Long departmentId);

    Long countByCategoryIdAndIsArchivedFalse(Long categoryId);

    // ─── Archived documents ───────────────────────────────────────────────────

    Page<Document> findAllByIsArchivedTrue(Pageable pageable);

    Page<Document> findByOwnerIdAndIsArchivedTrue(Long ownerId, Pageable pageable);
}