package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.controller.EmployeControllerImpl;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.exception.EmployeeException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.service.EmployeeServiceImpl;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Link;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@AutoConfigureMockMvc
@WebMvcTest(EmployeControllerImpl.class)
public class EmployeeControllerImplTest {
    private static final String API_END_POINT = "/api/v1/employee";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmployeeServiceImpl employeeService;

    List<Employee> employees;

    @BeforeEach
    void setUp() throws IOException {
        File file = new File("src/test/resources/test.json");
        employees = objectMapper.readValue(file, new TypeReference<>() {});
    }

    @Test
    public void testGetAllEmployees_ShouldReturnStatusOK() throws Exception {
        String expectedResponse = objectMapper.writeValueAsString(employees);
        Mockito.when(employeeService.findAll()).thenReturn(employees);
        mockMvc.perform(get(API_END_POINT))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andDo(print());

        ResultActions resultActions = mockMvc.perform(get(API_END_POINT))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));

        JSONAssert.assertEquals(
                expectedResponse, resultActions.andReturn().getResponse().getContentAsString(), false);
    }

    @Test
    public void testGetEmployeesByNameSearch_ShouldReturnStatusOK() throws Exception {
        String name = "Miss Mack Bruen";
        List<Employee> filteredEmployees =
                employees.stream().filter(e -> e.getName().contains(name)).toList();
        String expectedResponse = objectMapper.writeValueAsString(filteredEmployees);
        Mockito.when(employeeService.findAll()).thenReturn(filteredEmployees);

        ResultActions resultActions = mockMvc.perform(get(API_END_POINT + "/search/" + name))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andDo(print());

        JSONAssert.assertEquals(
                expectedResponse, resultActions.andReturn().getResponse().getContentAsString(), false);
    }

    @Test
    public void testGetEmployeesByNameSearch_ShouldReturnMultipleEmployeesAndStatusOK() throws Exception {
        String name = "Mack";
        List<Employee> filteredEmployees =
                employees.stream().filter(e -> e.getName().contains(name)).toList();
        String expectedResponse = objectMapper.writeValueAsString(filteredEmployees);
        Mockito.when(employeeService.findAll()).thenReturn(filteredEmployees);

        ResultActions resultActions = mockMvc.perform(get(API_END_POINT + "/search/" + name))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andDo(print());

        JSONAssert.assertEquals(
                expectedResponse, resultActions.andReturn().getResponse().getContentAsString(), false);
    }

    @Test
    public void testGetEmployeesByNameSearch_ShouldThrowEmployeeNotFoundException() throws Exception {
        String name = "William";
        List<Employee> filteredEmployees =
                employees.stream().filter(e -> e.getName().contains(name)).toList();
        Mockito.when(employeeService.findAll()).thenReturn(filteredEmployees);

        mockMvc.perform(get(API_END_POINT + "/search/" + name))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception);
                    assertInstanceOf(EmployeeNotFoundException.class, exception);
                })
                .andDo(print());
    }

    @Test
    public void testGetEmployeeById_WithExistingId_ShouldReturnStatusOK() throws Exception {
        String id = "21c49561-a15f-468f-9c0b-a0bcc0cc561b";
        Employee filteredEmployee = employees.stream()
                .filter(e -> Objects.equals(e.getId(), id))
                .findFirst()
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        Mockito.when(employeeService.findById(id)).thenReturn(filteredEmployee);

        mockMvc.perform(get(API_END_POINT + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filteredEmployee.getId()))
                .andExpect(jsonPath("$.employee_name").value(filteredEmployee.getName()))
                .andExpect(jsonPath("$._links").exists())
                .andDo(print());
    }

    @Test
    public void testGetEmployeeById_WithInValidId_ShouldThrowEmployeeNotFoundException() throws Exception {
        String id = "6b79d4a0-262e-4206-8c2b-c01222123";

        mockMvc.perform(get(API_END_POINT + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception);
                    assertInstanceOf(EmployeeNotFoundException.class, exception);
                })
                .andDo(print());
    }

    @Test
    public void testGetHighestSalaryOfEmployees_ShouldReturnStatusOK() throws Exception {
        Integer highestSalary = employees.stream()
                .map(Employee::getSalary)
                .max(Comparator.naturalOrder())
                .orElse(0);
        Mockito.when(employeeService.findAll()).thenReturn(employees);

        mockMvc.perform(get(API_END_POINT + "/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(highestSalary)))
                .andDo(print());
    }

    @Test
    public void testGetHighestSalaryOfEmployees_ShouldThrowEmployeeException() throws Exception {
        List<Employee> empytEmployeeList = new ArrayList<>();
        Mockito.when(employeeService.findAll()).thenReturn(empytEmployeeList);

        mockMvc.perform(get(API_END_POINT + "/highestSalary"))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception);
                    assertInstanceOf(EmployeeException.class, exception);
                })
                .andDo(print());
    }

    @Test
    public void testGetTopTenHighestEarningEmployee_ShouldReturnStatusOK() throws Exception {
        Mockito.when(employeeService.findAll()).thenReturn(employees);
        List<String> highestEarners = employeeService.findAll().stream()
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .toList();
        String expectedResponse = objectMapper.writeValueAsString(highestEarners);

        mockMvc.perform(get(API_END_POINT + "/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse))
                .andDo(print());
    }

    @Test
    public void testCreateEmployee_ShouldReturnThrowMethodArgumentNotValidException() throws Exception {
        Employee employee = employees.get(1);
        Mockito.when(employeeService.findAll()).thenReturn(employees);
        String expectedJson = objectMapper.writeValueAsString(employee);

        EmployeeRequest employeeRequest = objectMapper.readValue(expectedJson, EmployeeRequest.class);
        String requestJson = objectMapper.writeValueAsString(employeeRequest);

        System.out.println(expectedJson);

        mockMvc.perform(post(API_END_POINT).content(requestJson).contentType("application/json"))
                .andExpect(status().isAlreadyReported())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.employee_name").value(employee.getName()))
                .andExpect(jsonPath("$.employee_title").value(employee.getTitle()))
                .andExpect(jsonPath("$.employee_salary").value(employee.getSalary()))
                .andDo(print());
    }

    @Test
    public void testCreateEmployee_ShouldReturnAlreadyReportedStatus() throws Exception {
        Employee employee = employees.get(1);
        Mockito.when(employeeService.findAll()).thenReturn(employees);
        String expectedJson = objectMapper.writeValueAsString(employee);

        EmployeeRequest employeeRequest = objectMapper.readValue(expectedJson, EmployeeRequest.class);
        String requestJson = objectMapper.writeValueAsString(employeeRequest);

        System.out.println(expectedJson);

        mockMvc.perform(post(API_END_POINT).content(requestJson).contentType("application/json"))
                .andExpect(status().isAlreadyReported())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.employee_name").value(employee.getName()))
                .andExpect(jsonPath("$.employee_title").value(employee.getTitle()))
                .andExpect(jsonPath("$.employee_salary").value(employee.getSalary()))
                .andDo(print());
    }

    @Test
    public void testCreateEmployee_ShouldReturnStatusCreated() throws Exception {
        EmployeeRequest employeeRequest = new EmployeeRequest();
        employeeRequest.setName("William");
        employeeRequest.setAge(27);
        employeeRequest.setSalary(45832);
        employeeRequest.setTitle("Insurance Agent");

        String requestJson = objectMapper.writeValueAsString(employeeRequest);
        Employee createdEmployee = new Employee();
        createdEmployee.setId(UUID.randomUUID().toString());
        createdEmployee.setName("William");
        createdEmployee.setAge(27);
        createdEmployee.setSalary(45832);
        createdEmployee.setTitle("Insurance Agent");
        createdEmployee.setEmail("test@test.com");
        addLinks(createdEmployee);

        Mockito.when(employeeService.create(employeeRequest)).thenReturn(createdEmployee);

        mockMvc.perform(post(API_END_POINT).content(requestJson).contentType("application/json"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("$.id").value(createdEmployee.getId()))
                .andExpect(jsonPath("$.employee_name").value("William"))
                .andDo(print());
    }

    @Test
    public void testDeleteEmployeeById_ShouldReturnsStatusAccepted() throws Exception {
        Employee employee = employees.get(1);
        String expectedResponse = "Employee " + employee.getName() + " Deleted Successfully!";
        Mockito.when(employeeService.delete(employee)).thenReturn(expectedResponse);
        Mockito.when(employeeService.findAll()).thenReturn(employees);

        mockMvc.perform(delete(API_END_POINT + "/" + employee.getId()))
                .andExpect(status().isAccepted())
                .andExpect(content().string(expectedResponse))
                .andDo(print());
    }

    @Test
    public void testDeleteEmployeeById_ShouldThrowEmployeeNotFoundException() throws Exception {
        String invalidID = UUID.randomUUID().toString();
        Mockito.when(employeeService.findAll()).thenReturn(employees);

        mockMvc.perform(delete(API_END_POINT + "/" + invalidID))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    assertNotNull(exception);
                    assertInstanceOf(EmployeeNotFoundException.class, exception);
                })
                .andDo(print());
    }

    @Test
    public void testAddLinks_ShouldAddAllExpectedLinks() {
        Employee employee = new Employee();
        employee.setName("William");
        employee.setAge(27);
        employee.setSalary(45832);
        employee.setTitle("Insurance Agent");
        employee.setEmail("test@test.com");
        employee.setId(UUID.randomUUID().toString());

        addLinks(employee);
        List<Link> links = employee.getLinks().toList();

        assertTrue(links.stream().anyMatch(link -> link.getRel().value().equals("deleteById")));
        assertTrue(links.stream().anyMatch(link -> link.getRel().value().equals("searchByName")));
        assertTrue(links.stream().anyMatch(link -> link.getRel().value().equals("getEmployeeWithHighestSalary")));
        assertTrue(links.stream().anyMatch(link -> link.getRel().value().equals("getTopTenHighestSalaryOfEmployees")));
    }

    void addLinks(Employee employee) {
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
