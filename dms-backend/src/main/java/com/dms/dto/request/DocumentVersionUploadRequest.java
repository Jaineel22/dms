package com.dms.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersionUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @Size(max = 255, message = "Upload reason must not exceed 255 characters")
    private String uploadReason;
}