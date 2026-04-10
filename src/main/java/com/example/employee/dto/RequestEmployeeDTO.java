package com.example.employee.dto;

import com.example.employee.enums.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
@Schema(description = "Модель данных для запроса на создание и обновление сотрудника")
public record RequestEmployeeDTO(
        @Size(min = 2, max = 32, message = "Имя должно содержать от 2 до 32 символов")
        @NotBlank(message ="Введите имя")
        String firstName,

        @Size(min = 2, max = 32, message = "Фамилия должна содержать от 2 до 32 символов")
        @NotBlank(message= "Введите фамилию")
        String lastName,

        @Size(min = 2, max = 32, message = "Отчество должно содержать от 2 до 32 символов")
        String patronymic,

        @NotNull(message="Выберите отдел. IT, HR, FINANCE, MARKETING, LEGAL, SUPPORT")
        Department department,

        @Pattern(
                regexp = "^8\\d{10}$",
                message = "Номер телефона должен начинаться с 8 и содержать 11 цифр (например: 89190276543)"
        )
        @NotBlank(message="Укажите номер телефона")
        String numberPhone,

        @Email(message = "Некорректный формат email-адреса")
        @NotBlank(message="Укажите почту")
        String email,

        @NotBlank(message = "Укажите должность")
        @Size(min = 2, max = 64, message = "Должность должна содержать от 2 до 64 символов")
        String post
) {

}
