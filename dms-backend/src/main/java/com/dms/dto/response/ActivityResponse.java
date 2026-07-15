package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String action;
    private String entityType;
    private Long entityId;

    /** Best-effort display name for the entity (e.g. document title); may be null if unresolved. */
    private String entityName;

    private LocalDateTime createdAt;
}