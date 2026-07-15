package com.dms.service.impl;

import com.dms.dto.response.AuditLogResponse;
import com.dms.entity.AuditLog;
import com.dms.entity.User;
import com.dms.mapper.AuditLogMapper;
import com.dms.repository.AuditLogRepository;
import com.dms.repository.UserRepository;
import com.dms.service.AuditService;
import com.dms.util.AuditHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditHelper auditHelper;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void logAction(Long userId, String entityType, Long entityId, String action, Object oldValue, Object newValue) {
        HttpServletRequest request = currentHttpRequest();

        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setOldValue(auditHelper.toJson(oldValue));
        auditLog.setNewValue(auditHelper.toJson(newValue));
        auditLog.setIpAddress(auditHelper.getClientIp(request));
        auditLog.setUserAgent(auditHelper.getUserAgent(request));
        auditLog.setSessionId(auditHelper.getSessionId(request));
        auditLog.setRequestId(UUID.randomUUID().toString());
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
        log.debug("Audit log recorded: user={} entity={}:{} action={}", userId, entityType, entityId, action);
    }

    @Override
    @Transactional
    public void logAction(String entityType, Long entityId, String action, Object oldValue, Object newValue) {
        logAction(auditHelper.getCurrentUserId(), entityType, entityId, action, oldValue, newValue);
    }

    @Override
    public Page<AuditLogResponse> getAuditTrail(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(this::toEnrichedResponse);
    }

    @Override
    public Page<AuditLogResponse> getUserAuditTrail(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::toEnrichedResponse);
    }

    @Override
    public Page<AuditLogResponse> getAuditTrailByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::toEnrichedResponse);
    }

    @Override
    public Page<AuditLogResponse> getRecentActivity(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toEnrichedResponse);
    }

    private AuditLogResponse toEnrichedResponse(AuditLog auditLog) {
        AuditLogResponse response = auditLogMapper.toResponse(auditLog);
        userRepository.findById(auditLog.getUserId()).ifPresentOrElse(
                user -> {
                    response.setUserFullName(user.getFullName());
                    response.setUserEmail(user.getEmail());
                },
                () -> {
                    response.setUserFullName("Unknown User");
                    response.setUserEmail(null);
                }
        );
        return response;
    }

    private HttpServletRequest currentHttpRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (IllegalStateException e) {
            // No request context (e.g. scheduled job or async task calling logAction directly).
            return null;
        }
    }
}