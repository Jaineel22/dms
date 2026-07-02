package com.dms.mapper;

import com.dms.dto.request.WorkflowStepRequest;
import com.dms.dto.response.WorkflowStepResponse;
import com.dms.entity.WorkflowStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface WorkflowStepMapper {

    // ─── Entity → Response ────────────────────────────────────────────────────

    WorkflowStepResponse toResponse(WorkflowStep step);

    List<WorkflowStepResponse> toResponseList(List<WorkflowStep> steps);

    // ─── Request → Entity ─────────────────────────────────────────────────────

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive",  ignore = true)
    @Mapping(target = "workflow",  ignore = true)
    WorkflowStep toEntity(WorkflowStepRequest request);
}