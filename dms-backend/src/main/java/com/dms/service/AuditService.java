package com.dms.service;

import com.dms.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {

    void logAction(Long userId, String entityType, Long entityId, String action, Object oldValue, Object newValue);

    /** Uses the currently authenticated user as the actor. */
    void logAction(String entityType, Long entityId, String action, Object oldValue, Object newValue);

    Page<AuditLogResponse> getAuditTrail(String entityType, Long entityId, Pageable pageable);

    Page<AuditLogResponse> getUserAuditTrail(Long userId, Pageable pageable);

    Page<AuditLogResponse> getAuditTrailByAction(String action, Pageable pageable);

    Page<AuditLogResponse> getRecentActivity(Pageable pageable);
}