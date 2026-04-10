package com.example.employee.controller;

import com.example.employee.dto.RequestEmployeeDTO;
import com.example.employee.dto.ResponseEmployeeDTO;
import com.example.employee.enums.Department;
import com.example.employee.exception.EmailAlreadyExistsException;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.UUID;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(EmployeeController.class)
class TestEmployeeController {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testIdExists;
    private UUID testIdNotExists;
    private RequestEmployeeDTO requestDto;
    private ResponseEmployeeDTO responseDto;
    private Page<ResponseEmployeeDTO> pageResponse;

    @BeforeEach
    void setUp() {
        testIdExists = UUID.randomUUID();
        testIdNotExists = UUID.fromString("c4429ef4-89b1-4b52-be65-9b3b4f318144");
        requestDto = new RequestEmployeeDTO("Иван", "Иванов", "Иванович", Department.IT, "89001234567", "example@mail.ru", "Java Developer");
        responseDto = new ResponseEmployeeDTO(testIdExists, "Иван", "Иванов", "Иванович", Department.IT, "89001234567", "example@mail.ru", "Java Developer");
        pageResponse = new PageImpl<>(Collections.singletonList(responseDto));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee_WhenExists() throws Exception {
        given(employeeService.getEmployeeByID(testIdExists)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/employees/{id}", testIdExists)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testIdExists.toString()))
                .andExpect(jsonPath("$.firstName").value("Иван"));
    }

    @Test
    void getEmployeeById_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        given(employeeService.getEmployeeByID(testIdNotExists))
                .willThrow(new EmployeeNotFoundException("Сотрудник не найден"));

        mockMvc.perform(get("/api/v1/employees/{id}", testIdNotExists)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.field").value("id"))
                .andExpect(jsonPath("$.path").value("/api/v1/employees/" + testIdNotExists))
                .andExpect(jsonPath("$.errorCode").value("EMPLOYEE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Сотрудник не найден"));
    }

    @Test
    void getEmployeesWithPagination_ShouldReturnPageOfEmployees() throws Exception {
        given(employeeService.getEmployeesWithPagination(PageRequest.of(0, 5)))
                .willReturn(pageResponse);

        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Иван"));
    }

    @Test
    void getEmployeesWithPagination_ShouldReturnBadRequest_WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "-1")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATION"));

        then(employeeService).should(never()).getEmployeesWithPagination(any());
    }

    @Test
    void getEmployeesWithPagination_ShouldReturnBadRequest_WhenSizeIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "-5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONSTRAINT_VIOLATION"));

        then(employeeService).should(never()).getEmployeesWithPagination(any());
    }

    @Test
    void getEmployeesWithPagination_ShouldReturnBadRequest_WhenSizeIsTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "aaa")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TYPE_MISMATCH"));

        then(employeeService).should(never()).getEmployeesWithPagination(any());
    }

    @Test
    void getEmployeesWithPagination_ShouldReturnBadRequest_WhenPageIsTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "aaa")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TYPE_MISMATCH"));

        then(employeeService).should(never()).getEmployeesWithPagination(any());
    }

    @Test
    void searchEmployees_ShouldReturnFilteredEmployees() throws Exception {
        given(employeeService.searchEmployeesByNames("Иван", PageRequest.of(0, 5)))
                .willReturn(pageResponse);

        mockMvc.perform(get("/api/v1/employees/search")
                        .param("fullName", "Иван")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Иван"));
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        given(employeeService.saveEmployee(any(RequestEmployeeDTO.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("example@mail.ru"));
    }

    @Test
    void createEmployee_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        given(employeeService.saveEmployee(any(RequestEmployeeDTO.class)))
                .willThrow(new EmailAlreadyExistsException("Email уже используется"));

        mockMvc.perform(post("/api/v1/employees")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_EXISTS"))
                .andExpect(jsonPath("$.message").value("Email уже используется"));
    }

    @Test
    void deleteEmployee_ShouldReturnNoContent() throws Exception {
        willDoNothing().given(employeeService).deleteEmployee(testIdExists);

        mockMvc.perform(delete("/api/v1/employees/{id}", testIdExists))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteEmployee_ShouldReturnNotFound_WhenEmployeeDoesNotExist() throws Exception {
        willThrow(new EmployeeNotFoundException("Сотрудник не найден"))
                .given(employeeService).deleteEmployee(any(UUID.class));

        mockMvc.perform(delete("/api/v1/employees/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Сотрудник не найден"));
        }

    @Test
    void updateEmployee_ShouldReturnUpdatedEmployee() throws Exception {
        given(employeeService.updateEmployee(eq(testIdExists), any(RequestEmployeeDTO.class))).willReturn(responseDto);

        mockMvc.perform(put("/api/v1/employees/{id}", testIdExists)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Иванов"));
    }

    @Test
    void updateEmployee_ShouldReturnBadRequest_WhenPhoneNumberIsInvalid() throws Exception {
        RequestEmployeeDTO invalidDto = new RequestEmployeeDTO(
                "Иван", "Иванов", "Иванович", Department.IT,
                "123", "example@mail.ru", "Java Developer"
        );

        mockMvc.perform(put("/api/v1/employees/{id}", testIdExists)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Номер телефона должен начинаться с 8 и содержать 11 цифр (например: 89190276543)"));
    }
}