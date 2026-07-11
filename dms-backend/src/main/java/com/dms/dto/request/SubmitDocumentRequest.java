package com.dms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitDocumentRequest {

    @NotNull(message = "Document id is required")
    private Long documentId;

    /** Optional. If null, the user's default assigned workflow is used. */
    private Long workflowId;

    private String comments;
}