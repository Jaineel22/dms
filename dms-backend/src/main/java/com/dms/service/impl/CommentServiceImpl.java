package com.dms.service.impl;

import com.dms.dto.request.CommentRequest;
import com.dms.dto.response.CommentResponse;
import com.dms.entity.Comment;
import com.dms.entity.Document;
import com.dms.entity.User;
import com.dms.exception.DocumentException;
import com.dms.exception.WorkflowException;
import com.dms.mapper.CommentMapper;
import com.dms.repository.CommentRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.UserRepository;
import com.dms.util.SecurityUtils;
import com.dms.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public CommentResponse addComment(CommentRequest request) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new DocumentException("Document not found with id: " + request.getDocumentId()));

        Long currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new WorkflowException("Current user not found"));

        Comment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new WorkflowException(
                            "Parent comment not found with id: " + request.getParentCommentId()));
        }

        Comment comment = commentMapper.toEntity(request);
        comment.setDocument(document);
        comment.setUser(user);
        comment.setParentComment(parent);
        comment.setIsInternal(Boolean.TRUE.equals(request.getIsInternal()));

        Comment saved = commentRepository.save(comment);
        log.info("Comment {} added to document {} by user {}", saved.getId(), document.getId(), currentUserId);

        return commentMapper.toResponse(saved);
    }

    @Override
    public Page<CommentResponse> getDocumentComments(Long documentId, Pageable pageable) {
        return commentRepository.findByDocumentId(documentId, pageable)
                .map(commentMapper::toResponse);
    }

    @Override
    public CommentResponse getCommentThread(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new WorkflowException("Comment not found with id: " + commentId));
        return commentMapper.toResponse(comment);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new WorkflowException("Comment not found with id: " + commentId));

        Long currentUserId = securityUtils.getCurrentUserId();
        validateOwnerOrAdmin(comment, currentUserId);

        comment.setContent(content);
        Comment saved = commentRepository.save(comment);

        log.info("Comment {} updated by user {}", commentId, currentUserId);
        return commentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new WorkflowException("Comment not found with id: " + commentId));

        Long currentUserId = securityUtils.getCurrentUserId();
        validateOwnerOrAdmin(comment, currentUserId);

        commentRepository.delete(comment);
        log.info("Comment {} deleted by user {}", commentId, currentUserId);
    }

    private void validateOwnerOrAdmin(Comment comment, Long currentUserId) {
        boolean isOwner = comment.getUser() != null && comment.getUser().getId().equals(currentUserId);
        boolean isAdmin = securityUtils.hasRole("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new WorkflowException("Only the comment owner or an admin can perform this action");
        }
    }
}