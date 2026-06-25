package com.dms.mapper;

import com.dms.dto.request.DepartmentRequest;
import com.dms.dto.response.DepartmentResponse;
import com.dms.entity.Department;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DepartmentMapper {

    // ─── Entity → Response ────────────────────────────────────────────────────

    /**
     * Maps a {@link Department} to {@link DepartmentResponse}.
     * {@code userCount} must be set manually by the service layer
     * (requires a separate COUNT query).
     */
    @Mapping(target = "userCount", ignore = true)
    DepartmentResponse toResponse(Department department);

    /**
     * Convenience overload that also sets the pre-computed user count.
     */
    @Mapping(target = "userCount", source = "userCount")
    DepartmentResponse toResponse(Department department, Long userCount);

    // ─── CreateRequest → Entity ───────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive",  constant = "true")
    @Mapping(target = "users",     ignore = true)
    Department toEntity(DepartmentRequest request);

    // ─── UpdateRequest → Entity (patch) ──────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive",  ignore = true)
    @Mapping(target = "users",     ignore = true)
    void updateEntityFromRequest(DepartmentRequest request, @MappingTarget Department department);
}