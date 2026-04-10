package com.example.employee.controller;

import com.example.employee.dto.RequestEmployeeDTO;
import com.example.employee.dto.ResponseEmployeeDTO;
import com.example.employee.handler.ResponseError;
import com.example.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173/", allowedHeaders = "*")
@Tag(name = "Employee API",
    description = "Контроллер для управления данными сотрудников")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/employees")
@Validated
public class EmployeeController {

    private final EmployeeService service;

    @Operation(
        summary = "Получить сотрудника по ID",
        description = "Возвращает объект сотрудника, если он найден в системе"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Сотрудник найден",
            content = @Content(
                    mediaType="application/json",
                    schema = @Schema(implementation = ResponseEmployeeDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Сотрудник не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)
            )
    )
    @GetMapping("/{id}")
    public ResponseEntity<ResponseEmployeeDTO> getEmployeeById(@PathVariable UUID id){
        return ResponseEntity.ok(service.getEmployeeByID(id));
    }

    @Operation(
        summary = "Получить список сотрудников с учетом пагинации",
        description = "Возвращает указанное количество сотрудников на указанной странице"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Сотрудники получены с учетом всех параметров",
            content = @Content(
                    mediaType="application/json",
                    //Не совсем верный возвращаемый тип - должен быть Page<ResponseEmployeeDTO>
                    schema = @Schema(implementation = Page.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Допущена ошибка в запросе",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)
            )
    )
    @GetMapping
    public ResponseEntity<Page<ResponseEmployeeDTO>> getEmployeesWithPagination(
             @RequestParam(defaultValue = "0") @Min(0) int page,
             @RequestParam(defaultValue = "5") @Min(1) @Max(50) int size) {

        return ResponseEntity.ok(service.getEmployeesWithPagination(PageRequest.of(page, size)));
    }

    @Operation(
        summary = "Найти сотрудника(ов) по имени, фамилии или отчеству",
        description = "Возвращает сотрудника(ов) по заданным имени, фамилии, отчеству"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Сотрудник(и) найден с учетом всех параметров",
            content = @Content(
                    mediaType="application/json",
                    //Не совсем верный возвращаемый тип - должен быть Page<ResponseEmployeeDTO>
                    schema = @Schema(implementation = Page.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Допущена ошибка в запросе",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)
            )
    )
    @GetMapping("/search")
    public ResponseEntity<Page<ResponseEmployeeDTO>> searchEmployees(
            @RequestParam(required=false) String fullName,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int size) {

        return ResponseEntity.ok(service.searchEmployeesByNames(fullName, PageRequest.of(page, size)));
    }

    @Operation(
        summary = "Создать нового сотрудника",
        description = "Создает и возвращает нового сотрудника"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Сотрудник создан и отправлен в теле ответа",
            content = @Content(
                    mediaType="application/json",
                    schema = @Schema(implementation = ResponseEmployeeDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Сотрудник не создан",
            content = @Content(
                    mediaType ="application.json",
                    schema = @Schema(implementation = ResponseError.class)
            )
    )
    @PostMapping
    public ResponseEntity<ResponseEmployeeDTO> createEmployee(@Valid @RequestBody RequestEmployeeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.saveEmployee(dto));
    }

    @Operation(
        summary = "Удалить сотрудника из системы",
        description = "Удаляет сотрудника из системы"
    )
    @ApiResponse(
            responseCode = "204",
            description = "Сотрудник удален",
            content = @Content(
                    mediaType="application/json"
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Сотрудник не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)
            )
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        service.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Обновить данные сотрудника",
            description = "Обновляет данные существующего сотрудника"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Данные сотрудника обновлены",
            content = @Content(
                    mediaType="application/json",
                    schema = @Schema(implementation = ResponseEmployeeDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Сотрудник не найден",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Email или номер телефона уже заняты",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<ResponseEmployeeDTO> updateEmployee(@PathVariable UUID id, @Valid @RequestBody RequestEmployeeDTO dto) {
        return ResponseEntity.ok(service.updateEmployee(id, dto));
    }
}
