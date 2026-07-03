package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

    private Long id;
    private String documentNumber;
    private String title;
    private String description;
    private Integer currentVersion;
    private String status;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Boolean isConfidential;
    private LocalDate expiryDate;
    private String tags;
    private Boolean isArchived;
    private LocalDateTime archivedAt;

    private DepartmentResponse department;
    private DocumentCategoryResponse category;
    private UserResponse owner;
    private List<DocumentVersionResponse> versions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
}