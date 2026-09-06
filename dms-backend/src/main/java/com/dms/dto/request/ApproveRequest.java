package com.dms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveRequest {

    // Not client-supplied: ApprovalController always overwrites this from the
    // {id} path variable after @Valid has already run, so requiring it here
    // only rejected every well-formed request that (correctly) omitted it.
    private Long approvalId;

    private String comments;

    private MultipartFile attachment;
}