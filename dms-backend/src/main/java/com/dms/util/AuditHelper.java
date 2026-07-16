package com.dms.util;

import com.dms.entity.User;
import com.dms.repository.UserRepository;
import com.dms.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditHelper {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    public String getCurrentUserEmail() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).map(User::getEmail).orElse(null);
    }

    public String getCurrentUserFullName() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).map(User::getFullName).orElse(null);
    }

    public String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public String getUserAgent(HttpServletRequest request) {
        return request == null ? null : request.getHeader("User-Agent");
    }

    public String getSessionId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        return session == null ? null : session.getId();
    }

    public String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object for audit log: {}", e.getMessage());
            return null;
        }
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize audit log value: {}", e.getMessage());
            return null;
        }
    }

    public Map<String, Object> compareObjects(Object oldObj, Object newObj) {
        Map<String, Object> changes = new LinkedHashMap<>();
        if (oldObj == null && newObj == null) {
            return changes;
        }

        Map<String, Object> oldMap = oldObj == null
                ? Map.of()
                : objectMapper.convertValue(oldObj, new TypeReference<Map<String, Object>>() {
                });
        Map<String, Object> newMap = newObj == null
                ? Map.of()
                : objectMapper.convertValue(newObj, new TypeReference<Map<String, Object>>() {
                });

        Set<String> keys = new LinkedHashSet<>();
        keys.addAll(oldMap.keySet());
        keys.addAll(newMap.keySet());

        for (String key : keys) {
            Object oldVal = oldMap.get(key);
            Object newVal = newMap.get(key);
            if (!Objects.equals(oldVal, newVal)) {
                Map<String, Object> change = new LinkedHashMap<>();
                change.put("old", oldVal);
                change.put("new", newVal);
                changes.put(key, change);
            }
        }
        return changes;
    }
}