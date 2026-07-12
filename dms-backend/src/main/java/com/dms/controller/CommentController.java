package com.dms.controller;

import com.dms.constant.ApiConstants;
import com.dms.constant.RoleConstants;
import com.dms.dto.request.CommentRequest;
import com.dms.dto.response.ApiResponse;
import com.dms.dto.response.CommentResponse;
import com.dms.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Add and manage document comments and comment threads")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Add a comment to a document")
    @PostMapping(ApiConstants.COMMENTS_BASE)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addComment(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Comment added successfully"));
    }

    @Operation(summary = "Get paginated comments for a document")
    @GetMapping(ApiConstants.COMMENTS_DOCUMENT)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getDocumentComments(
            @PathVariable Long documentId, @PageableDefault(size = 20) Pageable pageable) {
        Page<CommentResponse> response = commentService.getDocumentComments(documentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get a comment and its full reply thread")
    @GetMapping(ApiConstants.COMMENTS_THREAD)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentThread(@PathVariable Long commentId) {
        CommentResponse response = commentService.getCommentThread(commentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update a comment (owner or admin only)")
    @PutMapping(ApiConstants.COMMENT_BY_ID)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id, @Valid @RequestBody UpdateCommentRequest request) {
        // Ownership vs. admin is enforced inside CommentServiceImpl.
        CommentResponse response = commentService.updateComment(id, request.getContent());
        return ResponseEntity.ok(ApiResponse.success(response, "Comment updated successfully"));
    }

    @Operation(summary = "Delete a comment (owner or admin only)")
    @DeleteMapping(ApiConstants.COMMENT_BY_ID)
    @PreAuthorize(RoleConstants.HAS_ROLE_USER_OR_ADMIN)
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        // Ownership vs. admin is enforced inside CommentServiceImpl.
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }

    /**
     * Small request body for PUT /comments/{id}. Not part of the Phase 4(b) DTO set
     * since CommentService.updateComment takes a raw content string; kept local to
     * this controller rather than adding a new top-level DTO file.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCommentRequest {
        @NotBlank(message = "Content is required")
        private String content;
    }
}