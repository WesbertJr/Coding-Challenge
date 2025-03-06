package com.reliaquest.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Employee name is required")
    private String name;

    @Positive(message = "Employee salary must be positive") @NotNull(message = "Employee salary is required") private Integer salary;

    @Min(value = 16, message = "Employee age must be at least 16")
    @Max(value = 75, message = "Employee age must be less than or equal to 75")
    @NotNull(message = "Employee age is required") private Integer age;

    @NotBlank(message = "Employee title is required")
    private String title;
}
