package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.ApiEndpoints;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.exception.EmployeeException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeControllerImpl implements IEmployeeController<Employee, EmployeeRequest> {

    private final EmployeeService employeeService;

    public EmployeControllerImpl(EmployeeService employeeService, ObjectMapper mapper) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.findAll();
        employees.forEach(this::addLinks);
        return new ResponseEntity<>(employeeService.findAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        List<Employee> employeesList = employeeService.findAll().stream()
                .filter(employee -> employee.getName().contains(searchString))
                .toList();
        if (employeesList.isEmpty()) {
            throw new EmployeeNotFoundException(searchString);
        }
        return new ResponseEntity<>(employeesList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@Valid @PathVariable String id) {
        Employee employee = employeeService.findById(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee with id " + id + " not found.");
        }
        addLinks(employee);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        List<Employee> employees = employeeService.findAll();
        if (employees.isEmpty()) {
            throw new EmployeeException("No employees found");
        }
        Integer highestSalary = employees.stream()
                .map(Employee::getSalary)
                .max(Comparator.naturalOrder())
                .get();
        return new ResponseEntity<>(highestSalary, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> topTen = employeeService.findAll().stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .toList();
        return new ResponseEntity<>(topTen, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequest employeeInput) {
        Optional<Employee> employeeOpt = employeeService.findAll().stream()
                .filter(e -> Objects.equals(e.getName(), employeeInput.getName()))
                .findFirst();

        if (employeeOpt.isPresent()) {
            return new ResponseEntity<>(employeeOpt.get(), HttpStatus.ALREADY_REPORTED);
        }
        Employee createdEmployee = employeeService.create(employeeInput);
        createdEmployee.add(Link.of(ApiEndpoints.BASE_URL.getUrl() + createdEmployee.getId())
                .withRel("getById"));
        createdEmployee.add(Link.of(ApiEndpoints.BASE_URL.getUrl() + "search/" + createdEmployee.getName())
                .withRel("searchByName"));
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@Valid @PathVariable String id) {
        Employee employee = employeeService.findAll().stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return new ResponseEntity<>(employeeService.delete(employee), HttpStatus.ACCEPTED);
    }

    private void addLinks(Employee employee) {
        if (employee.getLinks().stream().noneMatch(link -> link.getRel().value().equals("deleteById"))) {
            employee.add(
                    Link.of(ApiEndpoints.BASE_URL.getUrl() + employee.getId()).withRel("deleteById"));
        }
        if (employee.getLinks().stream().noneMatch(link -> link.getRel().value().equals("searchByName"))) {
            String name = employee.getName().split(" ")[0];
            employee.add(
                    Link.of(ApiEndpoints.BASE_URL.getUrl() + "search/" + name).withRel("searchByName"));
        }
        if (employee.getLinks().stream()
                .noneMatch(link -> link.getRel().value().equals("getEmployeeWithHighestSalary"))) {
            employee.add(
                    Link.of(ApiEndpoints.BASE_URL.getUrl() + "highestSalary").withRel("getEmployeeWithHighestSalary"));
        }
        if (employee.getLinks().stream()
                .noneMatch(link -> link.getRel().value().equals("getTopTenHighestSalaryOfEmployees"))) {
            employee.add(Link.of(ApiEndpoints.BASE_URL.getUrl() + "topTenHighestEarningEmployeeNames")
                    .withRel("getTopTenHighestSalaryOfEmployees"));
        }
    }
}
