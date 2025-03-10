package com.reliaquest.api.service;

import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeRequest;
import java.util.List;

public interface EmployeeService {

    Employee create(EmployeeRequest request);

    String delete(String id);

    List<Employee> findAll();

    Employee findById(String id);

    List<Employee> findByName(String name);

    List<String> findTopTenHighestEarningEmployeeNames();

    Integer getHighestSalaryOfEmployees();
}
