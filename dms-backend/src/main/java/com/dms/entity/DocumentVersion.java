package com.dms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Immutable version snapshot for a {@link Document}.
 *
 * <p>Does NOT extend {@link BaseEntity} because:
 * <ul>
 *   <li>Version records are append-only — no {@code updated_at} / {@code updated_by} makes sense.</li>
 *   <li>There is no {@code is_active} flag — all version rows are permanent records.</li>
 *   <li>The table schema has only a single {@code created_at} timestamp.</li>
 * </ul>
 * The {@code id} and {@code createdAt} are managed locally, consistent with the blueprint.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "document_versions",
    uniqueConstraints = @UniqueConstraint(
        name        = "uk_document_version",
        columnNames = {"document_id", "version_number"}
    ),
    indexes = {
        @Index(name = "idx_version_document",    columnList = "document_id"),
        @Index(name = "idx_version_uploaded_by", columnList = "uploaded_by")
    }
)
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "upload_reason", length = 255)
    private String uploadReason;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ─── Associations ─────────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
}