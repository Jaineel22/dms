package com.dms.mapper;

import com.dms.dto.request.DocumentVersionUploadRequest;
import com.dms.dto.response.DocumentVersionResponse;
import com.dms.entity.DocumentVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {UserMapper.class}
)
public interface DocumentVersionMapper {

    // ─── Entity → Response ────────────────────────────────────────────────────

    @Mapping(target = "uploadedBy", source = "uploadedBy")
    DocumentVersionResponse toResponse(DocumentVersion version);

    List<DocumentVersionResponse> toResponseList(List<DocumentVersion> versions);

    // ─── Request → Entity skeleton (file fields set by service) ──────────────

    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "versionNumber", ignore = true)
    @Mapping(target = "filePath",      ignore = true)
    @Mapping(target = "fileName",      ignore = true)
    @Mapping(target = "fileSize",      ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "document",      ignore = true)
    @Mapping(target = "uploadedBy",    ignore = true)
    DocumentVersion toEntity(DocumentVersionUploadRequest request);
}