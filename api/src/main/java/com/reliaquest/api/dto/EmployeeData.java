package com.reliaquest.api.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeData {
    private List<Employee> data;
    private String status;
}
