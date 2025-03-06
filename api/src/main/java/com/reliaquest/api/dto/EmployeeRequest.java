package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @JsonProperty("employee_name")
    private String name;

    @Positive(message = "Employee salary must be positive") @NotNull(message = "Employee salary is required") @JsonProperty("employee_salary")
    private Integer salary;

    @Min(value = 16, message = "Employee age must be at least 16")
    @Max(value = 75, message = "Employee age must be less than or equal to 75")
    @NotNull(message = "Employee age is required") @JsonProperty("employee_age")
    private Integer age;

    @NotBlank(message = "Employee title is required")
    @JsonProperty("employee_title")
    private String title;
}