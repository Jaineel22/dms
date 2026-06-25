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
public class UserCreateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    @NotBlank(message = "Employee ID is required")
    @Size(max = 20, message = "Employee ID must not exceed 20 characters")
    @Pattern(
        regexp = "^[A-Z0-9]+$",
        message = "Employee ID must contain only uppercase letters and digits"
    )
    private String employeeId;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    @NotNull(message = "Department ID is required")
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
    @Builder.Default
    private Integer employeeLevel = 1;
}