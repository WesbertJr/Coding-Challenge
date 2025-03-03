package com.reliaquest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeData;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.exception.EmployeeException;
import java.util.List;
import java.util.Objects;
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

    public EmployeeServiceImpl(ObjectMapper mapper) {
        this.mapper = mapper;
        restClient = RestClient.builder()
                .baseUrl("http://localhost:8112/api/v1/")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    @Cacheable("employees")
    public List<Employee> findAll() {
        EmployeeData payload = restClient.get().uri("/employee").retrieve().body(EmployeeData.class);
        List<Employee> employeeList = Objects.requireNonNull(payload).getData();
        log.info("fetched: {}", employeeList);
        return employeeList;
    }

    @Override
    @Cacheable("employees")
    public Employee findById(String id) {
        String json = restClient.get().uri("/employee/" + id).retrieve().body(String.class);
        try {
            Employee employee = mapper.treeToValue(mapper.readTree(json).path("data"), Employee.class);
            log.info("fetched: {}", employee);
            return employee;
        } catch (JsonProcessingException e) {
            throw new EmployeeException("Error processing JSON: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public Employee create(EmployeeRequest request) {
        String payload = restClient
                .method(HttpMethod.POST)
                .uri("/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
        try {
            return mapper.treeToValue(mapper.readTree(payload).path("data"), Employee.class);
        } catch (JsonProcessingException e) {
            throw new EmployeeException("Error processing JSON: " + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public String delete(Employee employee) {
        ObjectNode json = mapper.createObjectNode();
        json.put("name", employee.getName());
        json.put("age", employee.getAge());
        json.put("salary", employee.getSalary());
        json.put("title", employee.getTitle());
        ResponseEntity<Void> response = restClient
                .method(HttpMethod.DELETE)
                .uri("/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toBodilessEntity();

        if (response.getStatusCode().is2xxSuccessful()) {
            return "Employee " + employee.getName() + " Deleted Successfully!";
        } else {
            return "Failed to delete Employee: " + employee.getName();
        }
    }
}
