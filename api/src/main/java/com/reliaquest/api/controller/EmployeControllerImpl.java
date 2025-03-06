package com.reliaquest.api.controller;

import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
@Validated
public class EmployeControllerImpl implements IEmployeeController<Employee, EmployeeRequest> {

    private final EmployeeService employeeService;

    public EmployeControllerImpl(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.findAll();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@Valid @PathVariable String id) {
        Employee employee = employeeService.findById(id);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@Valid @PathVariable String searchString) {
        List<Employee> employeesList = employeeService.findByName(searchString);
        return new ResponseEntity<>(employeesList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();
        return new ResponseEntity<>(highestSalary, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> topTen = employeeService.findTopTenHighestEarningEmployeeNames();
        return new ResponseEntity<>(topTen, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequest employeeInput) {
        Employee createdEmployee = employeeService.create(employeeInput);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@Valid @PathVariable String id) {
        String msg = employeeService.delete(id);
        return new ResponseEntity<>(msg, HttpStatus.ACCEPTED);
    }
}
