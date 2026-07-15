package com.dms.mapper;

import com.dms.dto.response.AuditLogResponse;
import com.dms.entity.AuditLog;
import com.dms.util.AuditHelper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * userFullName / userEmail are intentionally left unset here: AuditLog stores only a raw
 * userId (no JPA relationship, by design — see Phase 5(a) notes), so enrichment with the
 * user's display name/email happens in AuditServiceImpl after this mapping runs.
 */
@Mapper(componentModel = "spring")
public abstract class AuditLogMapper {

    @Autowired
    protected AuditHelper auditHelper;

    @Mapping(target = "userFullName", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    @Mapping(target = "oldValue", source = "oldValue", qualifiedByName = "jsonToMap")
    @Mapping(target = "newValue", source = "newValue", qualifiedByName = "jsonToMap")
    public abstract AuditLogResponse toResponse(AuditLog auditLog);

    public abstract List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs);

    @Named("jsonToMap")
    @SuppressWarnings("unchecked")
    protected Map<String, Object> jsonToMap(String json) {
        return auditHelper.fromJson(json, Map.class);
    }
}