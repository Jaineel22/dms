package com.dms.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    private Long roleId;

    private Long departmentId;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Pattern(
        regexp = "^(\\+?[1-9]\\d{1,14})?$",
        message = "Phone number must be a valid international format"
    )
    private String phoneNumber;

    private Long managerId;

    @Min(value = 1, message = "Employee level must be between 1 and 4")
    @Max(value = 4, message = "Employee level must be between 1 and 4")
    private Integer employeeLevel;

    private Boolean isActive;
}