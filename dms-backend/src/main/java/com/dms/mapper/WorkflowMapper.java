package com.dms.mapper;

import com.dms.dto.request.WorkflowCreateRequest;
import com.dms.dto.request.WorkflowUpdateRequest;
import com.dms.dto.response.WorkflowResponse;
import com.dms.entity.WorkflowDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {DepartmentMapper.class, WorkflowStepMapper.class}
)
public interface WorkflowMapper {

    // ─── Entity → Response ────────────────────────────────────────────────────

    @Mapping(target = "steps", source = "steps")
    WorkflowResponse toResponse(WorkflowDefinition workflow);

    // ─── CreateRequest → Entity ───────────────────────────────────────────────

    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    @Mapping(target = "createdBy",     ignore = true)
    @Mapping(target = "updatedBy",     ignore = true)
    @Mapping(target = "isActive",      constant = "true")
    @Mapping(target = "department",    ignore = true)
    @Mapping(target = "steps",         ignore = true)
    @Mapping(target = "userWorkflows", ignore = true)
    WorkflowDefinition toEntity(WorkflowCreateRequest request);

    // ─── UpdateRequest → Entity (patch) ──────────────────────────────────────

    @Mapping(target = "id",            ignore = true)
    @Mapping(target = "createdAt",     ignore = true)
    @Mapping(target = "updatedAt",     ignore = true)
    @Mapping(target = "createdBy",     ignore = true)
    @Mapping(target = "updatedBy",     ignore = true)
    @Mapping(target = "department",    ignore = true)
    @Mapping(target = "steps",         ignore = true)
    @Mapping(target = "userWorkflows", ignore = true)
    void updateEntityFromRequest(WorkflowUpdateRequest request, @MappingTarget WorkflowDefinition workflow);
}