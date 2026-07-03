package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersionResponse {

    private Long id;
    private Integer versionNumber;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String uploadReason;
    private LocalDateTime createdAt;
    private UserResponse uploadedBy;
}