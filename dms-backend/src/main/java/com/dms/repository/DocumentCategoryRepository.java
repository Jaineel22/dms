package com.dms.repository;

import com.dms.entity.DocumentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {

    // ─── Lookup ───────────────────────────────────────────────────────────────

    Optional<DocumentCategory> findByName(String name);

    // ─── Existence checks ────────────────────────────────────────────────────

    Boolean existsByName(String name);

    /** Exclude self during update uniqueness check. */
    Boolean existsByNameAndIdNot(String name, Long id);

    Boolean existsByIdAndIsActiveTrue(Long id);

    // ─── List queries ─────────────────────────────────────────────────────────

    List<DocumentCategory> findAllByIsActiveTrue();

    Page<DocumentCategory> findAllByIsActiveTrue(Pageable pageable);
}