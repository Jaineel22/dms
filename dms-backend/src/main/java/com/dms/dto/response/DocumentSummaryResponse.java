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
public class DocumentSummaryResponse {

    private long totalDocuments;
    private long draftDocuments;
    private long uploadedDocuments;
    private long readyForSubmissionDocuments;
    private long archivedDocuments;
    private long totalStorageSize;
}