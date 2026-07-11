package com.dms.service;

import com.dms.dto.request.CommentRequest;
import com.dms.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    CommentResponse addComment(CommentRequest request);

    Page<CommentResponse> getDocumentComments(Long documentId, Pageable pageable);

    CommentResponse getCommentThread(Long commentId);

    CommentResponse updateComment(Long commentId, String content);

    void deleteComment(Long commentId);
}