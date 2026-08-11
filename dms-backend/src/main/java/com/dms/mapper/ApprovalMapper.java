package com.dms.mapper;

import com.dms.dto.response.ApprovalResponse;
import com.dms.entity.Approval;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApprovalMapper {

    @Mapping(target = "workflowInstanceId", source = "workflowInstance.id")
    @Mapping(target = "stepId", source = "step.id")
    @Mapping(target = "stepName", source = "step.roleName")
    @Mapping(target = "approverId", source = "approver.id")
    @Mapping(target = "approverName", expression = "java(approval.getApprover().getFirstName() + \" \" + approval.getApprover().getLastName())")
    ApprovalResponse toResponse(Approval approval);

    List<ApprovalResponse> toResponseList(List<Approval> approvals);
}