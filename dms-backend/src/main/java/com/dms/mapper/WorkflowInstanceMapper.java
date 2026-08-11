package com.dms.mapper;

import com.dms.dto.response.PendingApprovalResponse;
import com.dms.dto.response.WorkflowInstanceResponse;
import com.dms.entity.Document;
import com.dms.entity.WorkflowInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface WorkflowInstanceMapper {

    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "workflowDefinitionId", source = "workflowDefinition.id")
    @Mapping(target = "workflowName", source = "workflowDefinition.name")
    @Mapping(target = "currentStepId", source = "currentStep.id")
    @Mapping(target = "currentStepName", source = "currentStep.roleName")
    @Mapping(target = "currentApproverId", source = "currentApprover.id")
    @Mapping(target = "currentApproverName", expression = "java(workflowInstance.getCurrentApprover() != null ? workflowInstance.getCurrentApprover().getFirstName() + \" \" + workflowInstance.getCurrentApprover().getLastName() : null)")
    @Mapping(target = "submittedBy", source = "submittedBy.id")
    WorkflowInstanceResponse toResponse(WorkflowInstance workflowInstance);

    @Mapping(target = "id", source = "workflowInstance.id")
    @Mapping(target = "documentId", source = "document.id")
    @Mapping(target = "documentTitle", source = "document.title")
    @Mapping(target = "documentNumber", source = "document.documentNumber")
    @Mapping(target = "stepId", source = "workflowInstance.currentStep.id")
    @Mapping(target = "stepName", source = "workflowInstance.currentStep.roleName")
    @Mapping(target = "stepNumber", source = "workflowInstance.currentStep.stepNumber")
    @Mapping(target = "submittedBy", source = "workflowInstance.submittedBy.id")
    @Mapping(target = "submittedByName", expression = "java(workflowInstance.getSubmittedBy().getFirstName() + \" \" + workflowInstance.getSubmittedBy().getLastName())")
    @Mapping(target = "submittedAt", source = "workflowInstance.submittedAt")
    @Mapping(target = "deadlineAt", source = "workflowInstance.deadlineAt")
    @Mapping(target = "daysRemaining", expression = "java(calculateDaysRemaining(workflowInstance.getDeadlineAt()))")
    PendingApprovalResponse toPendingApprovalResponse(WorkflowInstance workflowInstance, Document document);

    default Long calculateDaysRemaining(LocalDateTime deadlineAt) {
        if (deadlineAt == null) {
            return null;
        }
        return Duration.between(LocalDateTime.now(), deadlineAt).toDays();
    }
}