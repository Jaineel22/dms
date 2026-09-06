package com.dms.service.impl;

import com.dms.dto.request.CommentRequest;
import com.dms.dto.response.CommentResponse;
import com.dms.entity.Comment;
import com.dms.entity.Document;
import com.dms.entity.Notification;
import com.dms.entity.User;
import com.dms.exception.AccessDeniedException;
import com.dms.exception.DocumentException;
import com.dms.exception.WorkflowException;
import com.dms.mapper.CommentMapper;
import com.dms.repository.CommentRepository;
import com.dms.repository.DocumentRepository;
import com.dms.repository.UserRepository;
import com.dms.util.SecurityUtils;
import com.dms.service.CommentService;
import com.dms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

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

        notifyParticipants(document, currentUserId);

        return commentMapper.toResponse(saved);
    }

    /**
     * Notifies the document owner plus everyone who has previously commented on the
     * document (the commenter themselves is always excluded). Best-effort: a delivery
     * failure must never break the comment transaction.
     */
    private void notifyParticipants(Document document, Long commenterId) {
        Set<Long> recipients = new LinkedHashSet<>();

        if (document.getOwner() != null) {
            recipients.add(document.getOwner().getId());
        }
        for (Comment previous : commentRepository.findByDocumentIdOrderByCreatedAtAsc(document.getId())) {
            if (previous.getUser() != null) {
                recipients.add(previous.getUser().getId());
            }
        }
        recipients.remove(commenterId);

        String title = "New comment";
        String message = "A new comment was added to \"" + document.getTitle() + "\".";
        String link = "/documents/" + document.getId();

        for (Long userId : recipients) {
            try {
                notificationService.createNotificationForUser(
                        userId, Notification.Type.COMMENT_ADDED, title, message, link);
            } catch (Exception e) {
                log.warn("Failed to send COMMENT_ADDED notification to user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getDocumentComments(Long documentId, Pageable pageable) {
        return commentRepository.findByDocumentId(documentId, pageable)
                .map(commentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
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
            throw new AccessDeniedException("Only the comment owner or an admin can perform this action");
        }
    }
}