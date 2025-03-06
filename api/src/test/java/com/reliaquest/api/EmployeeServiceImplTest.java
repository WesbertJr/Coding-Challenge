package com.reliaquest.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.exception.EmployeeException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.service.EmployeeCacheService;
import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.api.service.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeCacheService cacheService;

    List<Employee> employees;

    ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        File file = new File("src/test/resources/test.json");
        employees = objectMapper.readValue(file, new TypeReference<>() {});
    }

    @Test
    public void testGetEmployeeById_EmployeeNotFound_ShouldThrowException() {
        String invalidId = "999";

        when(cacheService.getEmployeeData()).thenReturn(null);
        when(employeeService.findById(invalidId)).thenThrow(new EmployeeNotFoundException(invalidId));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.findById(invalidId);
        });

        assertEquals("Unable to find employee with id or name: " + invalidId, exception.getMessage());
    }

    @Test
    public void testDeleteEmployee_EmployeeNotFound_ShouldThrowException() {
        String invalidId = "888";

        when(employeeService.delete(invalidId)).thenThrow(new EmployeeNotFoundException(invalidId));

        Exception exception = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.delete(invalidId);
        });

        assertEquals("Unable to find employee with id or name: " + invalidId, exception.getMessage());
    }
}
