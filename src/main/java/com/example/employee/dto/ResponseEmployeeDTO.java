package com.example.employee.dto;

import com.example.employee.entity.EmployeeEntity;
import com.example.employee.enums.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.UUID;

@Builder
@Schema(description = "Модель данных для ответа на запрос")
public record ResponseEmployeeDTO(

        UUID id,
        String firstName,
        String lastName,
        String patronymic,
        Department department,
        String numberPhone,
        String email,
        String post
) {

    public static ResponseEmployeeDTO from(EmployeeEntity entity) {
        return ResponseEmployeeDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .patronymic(entity.getPatronymic())
                .department(entity.getDepartment())
                .numberPhone(entity.getNumberPhone())
                .email(entity.getEmail())
                .post(entity.getPost())
                .build();
    }
}
