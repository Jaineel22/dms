package com.dms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name    = "documents",
    indexes = {
        @Index(name = "idx_document_number",     columnList = "document_number"),
        @Index(name = "idx_document_status",     columnList = "status"),
        @Index(name = "idx_document_owner",      columnList = "owner_id"),
        @Index(name = "idx_document_category",   columnList = "category_id"),
        @Index(name = "idx_document_department", columnList = "department_id"),
        @Index(name = "idx_document_archived",   columnList = "is_archived")
    }
)
public class Document extends BaseEntity {

    @Column(name = "document_number", unique = true, nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "current_version", nullable = false)
    private Integer currentVersion = 1;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private String status = "DRAFT";

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Builder.Default
    @Column(name = "is_confidential")
    private Boolean isConfidential = false;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "tags", length = 500)
    private String tags;

    @Builder.Default
    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private DocumentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Builder.Default
    @OneToMany(
        mappedBy      = "document",
        fetch         = FetchType.LAZY,
        cascade       = CascadeType.ALL,
        orphanRemoval = true
    )
    @OrderBy("versionNumber DESC")
    private List<DocumentVersion> versions = new ArrayList<>();
}