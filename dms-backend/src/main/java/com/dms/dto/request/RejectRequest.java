package com.dms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectRequest {

    @NotNull(message = "Approval id is required")
    private Long approvalId;

    @NotBlank(message = "Comments are required when rejecting")
    private String comments;

    private MultipartFile attachment;
}