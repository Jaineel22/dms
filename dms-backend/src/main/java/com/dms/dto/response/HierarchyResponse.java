package com.dms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String employeeId;
    private Integer employeeLevel;
    private String designation;
    private Long managerId;
    private String managerName;
    private Integer managerLevel;
    private String departmentName;
}