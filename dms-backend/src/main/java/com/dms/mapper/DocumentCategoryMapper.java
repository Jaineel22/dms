package com.dms.mapper;

import com.dms.dto.request.DocumentCategoryRequest;
import com.dms.dto.response.DocumentCategoryResponse;
import com.dms.entity.DocumentCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DocumentCategoryMapper {

    // ─── Entity → Response ────────────────────────────────────────────────────

    /** documentCount must be set manually by the service layer. */
    @Mapping(target = "documentCount", ignore = true)
    DocumentCategoryResponse toResponse(DocumentCategory category);

    /** Convenience overload — sets the pre-computed document count. */
    @Mapping(target = "documentCount", source = "documentCount")
    DocumentCategoryResponse toResponse(DocumentCategory category, Long documentCount);

    List<DocumentCategoryResponse> toResponseList(List<DocumentCategory> categories);

    // ─── Request → Entity ─────────────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive",  constant = "true")
    @Mapping(target = "documents", ignore = true)
    DocumentCategory toEntity(DocumentCategoryRequest request);

    // ─── Update → Entity (patch) ──────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "documents", ignore = true)
    void updateEntityFromRequest(DocumentCategoryRequest request, @MappingTarget DocumentCategory category);
}