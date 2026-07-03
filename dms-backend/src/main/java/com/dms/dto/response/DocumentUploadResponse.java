package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {

    private Long documentId;
    private String documentNumber;
    private String fileName;
    private Long fileSize;
    private Integer versionNumber;
    private String status;
    private String message;
}