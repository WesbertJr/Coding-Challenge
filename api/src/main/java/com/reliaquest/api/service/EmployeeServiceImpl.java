package com.reliaquest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeData;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.exception.EmployeeException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final RestClient restClient;
    private final ObjectMapper mapper;
    private final EmployeeCacheService cacheService;

    public EmployeeServiceImpl(ObjectMapper mapper, EmployeeCacheService cacheService) {
        this.mapper = mapper;
        this.cacheService = cacheService;
        restClient = RestClient.builder()
                .baseUrl("http://localhost:8112/api/v1/")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public List<Employee> findAll() {
        EmployeeData employees = cacheService.getEmployeeData();

        List<Employee> employeeList = employees.getData();

        log.info("fetched: {}", employeeList);

        return employeeList;
    }

    @Override
    @Cacheable("employees")
    public Employee findById(String id) {
        try {
            String json = restClient.get().uri("/employee/" + id).retrieve().body(String.class);
            Employee employee = mapper.treeToValue(mapper.readTree(json).path("data"), Employee.class);
            log.info("fetched: {}", employee);

            return employee;

        } catch (Exception e) {
            throw new EmployeeNotFoundException(id);
        }
    }

    @Override
    public List<Employee> findByName(String name) {
        EmployeeData employees = cacheService.getEmployeeData();
        List<Employee> employeeList = employees.getData().stream()
                .filter(employee -> employee.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();

        if (employeeList.isEmpty()) {
            throw new EmployeeNotFoundException(name);
        }

        log.info("fetched: {}", employeeList);

        return employeeList;
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        List<Employee> employees = cacheService.getEmployeeData().getData();
        try {
            Integer highestSalary = employees.stream()
                    .map(Employee::getSalary)
                    .max(Comparator.naturalOrder())
                    .orElse(0);

            log.info("fetched: {}", highestSalary);

            return highestSalary;
        } catch (Exception e) {
            throw new EmployeeException("Unable to fetch highest salary of employees: " + e.getMessage());
        }
    }

    @Override
    public List<String> findTopTenHighestEarningEmployeeNames() {
        EmployeeData employees = cacheService.getEmployeeData();

        List<String> topTen = employees.getData().stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .toList();

        log.info("fetched: {}", topTen);

        return topTen;
    }

    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public Employee create(EmployeeRequest request) {
        ObjectNode json = mapper.createObjectNode();
        json.put("name", request.getName());
        json.put("salary", request.getSalary());
        json.put("title", request.getTitle());
        json.put("age", request.getAge());

        EmployeeData employees = cacheService.getEmployeeData();
        Optional<Employee> employeeOpt = employees.getData().stream()
                .filter(e -> Objects.equals(e.getName(), request.getName()))
                .findFirst();

        if (employeeOpt.isPresent()) {
            throw new EmployeeException("Employee already exists");
        }

        try {
            String payload = restClient
                    .method(HttpMethod.POST)
                    .uri("/employee")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(String.class);

            return mapper.treeToValue(mapper.readTree(payload).path("data"), Employee.class);

        } catch (JsonProcessingException e) {
            throw new EmployeeException("Error processing JSON: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public String delete(String id) {
        Employee employee = cacheService.getEmployeeData().getData().stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        ObjectNode json = mapper.createObjectNode();
        json.put("name", employee.getName());

        ResponseEntity<Void> response = restClient
                .method(HttpMethod.DELETE)
                .uri("/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toBodilessEntity();

        if (response.getStatusCode().is2xxSuccessful()) {
            return "Employee " + employee.getName() + " with id: " + employee.getId() + " Deleted Successfully!";
        } else {
            return "Failed to delete Employee: " + employee.getName();
        }
    }
}
