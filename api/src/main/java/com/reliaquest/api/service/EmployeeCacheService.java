package com.reliaquest.api.service;

import com.reliaquest.api.dto.EmployeeData;
import com.reliaquest.api.exception.EmployeeException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EmployeeCacheService {

    private final RestClient restClient;

    public EmployeeCacheService() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8112/api/v1/")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Cacheable("employees")
    public EmployeeData getEmployeeData() {
        EmployeeData payload = restClient.get().uri("/employee").retrieve().body(EmployeeData.class);
        if (payload == null || payload.getData().isEmpty()) {
            throw new EmployeeException("No Employees Returned!");
        }
        return payload;
    }
}
