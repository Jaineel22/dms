package com.dms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignManagerRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    private String reason;
}