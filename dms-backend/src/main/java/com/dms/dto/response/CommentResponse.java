package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long documentId;
    private Long userId;
    private String userName;
    private String content;
    private Boolean isInternal;
    private Long parentCommentId;

    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}