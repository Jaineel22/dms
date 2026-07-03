package com.dms.repository;

import com.dms.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    // ─── List queries ─────────────────────────────────────────────────────────

    /** Returns all versions for a document, newest first. */
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(Long documentId);

    /** Returns all versions uploaded by a specific user. */
    List<DocumentVersion> findByUploadedById(Long userId);

    // ─── Single-record lookup ────────────────────────────────────────────────

    Optional<DocumentVersion> findByDocumentIdAndVersionNumber(Long documentId, Integer versionNumber);

    /** Returns the most recent (highest version number) snapshot. */
    Optional<DocumentVersion> findTopByDocumentIdOrderByVersionNumberDesc(Long documentId);

    // ─── Count ────────────────────────────────────────────────────────────────

    Integer countByDocumentId(Long documentId);

    // ─── Delete ───────────────────────────────────────────────────────────────

    /**
     * Hard-deletes all version records for a document.
     * This should rarely be called directly; prefer letting {@code CascadeType.ALL}
     * on {@link com.dms.entity.Document#versions} handle cascade deletion when
     * the parent document itself is deleted.
     */
    @Modifying
    @Query("DELETE FROM DocumentVersion v WHERE v.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    // ─── Existence check ──────────────────────────────────────────────────────

    Boolean existsByDocumentIdAndVersionNumber(Long documentId, Integer versionNumber);

    // ─── Next version helper ─────────────────────────────────────────────────

    /**
     * Returns the highest existing version number for the given document.
     * Returns 0 (via coalesce in application code) when no versions exist yet.
     */
    @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM DocumentVersion v WHERE v.document.id = :documentId")
    Integer findMaxVersionNumberByDocumentId(@Param("documentId") Long documentId);
}