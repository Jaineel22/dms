package com.dms.mapper;

import com.dms.dto.request.CommentRequest;
import com.dms.dto.response.CommentResponse;
import com.dms.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    // `replies` (List<Comment> -> List<CommentResponse>) is resolved automatically by
    // MapStruct using this same toResponse(Comment) method.
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "document", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Comment toEntity(CommentRequest request);
}