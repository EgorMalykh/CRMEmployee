package com.example.employee.service;

import com.example.employee.dto.RequestEmployeeDTO;
import com.example.employee.dto.ResponseEmployeeDTO;
import com.example.employee.entity.EmployeeEntity;
import com.example.employee.exception.EmailAlreadyExistsException;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.exception.NumberPhoneAlreadyExistsException;
import com.example.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;

    @Transactional(readOnly = true)
    public ResponseEmployeeDTO getEmployeeByID (UUID id){
        return repository.findById(id)
                .map(ResponseEmployeeDTO::from)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Сотрудник с id %s не найден", id)));
    }

    @Transactional(readOnly = true)
    public Page<ResponseEmployeeDTO> getEmployeesWithPagination(Pageable pageable) {
        return repository.findAll(pageable).map(ResponseEmployeeDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<ResponseEmployeeDTO> searchEmployeesByNames(String searchTerm, Pageable pageable) {
        return repository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPatronymicContainingIgnoreCase(
                searchTerm, searchTerm, searchTerm, pageable
        ).map(ResponseEmployeeDTO::from);
    }

    @Transactional
    public ResponseEmployeeDTO saveEmployee(RequestEmployeeDTO dto) {
        if (repository.existsByNumberPhone(dto.numberPhone())) {
            throw new NumberPhoneAlreadyExistsException("Данный номер телефона занят");
        }
        if (repository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Данный email занят");
        }

        return ResponseEmployeeDTO.from(repository.save(EmployeeEntity.from(dto)));
    }

    @Transactional
    public void deleteEmployee(UUID id){
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(String.format("Сотрудник с id %s не найден", id));
        }
        repository.deleteById(id);
    }

    @Transactional
    public ResponseEmployeeDTO updateEmployee(UUID id, RequestEmployeeDTO dto) {
        EmployeeEntity entity = repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(String.format("Сотрудник с id %s не найден", id)));

        if (!entity.getEmail().equals(dto.email()) && repository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Данный email занят");
        }

        if (!entity.getNumberPhone().equals(dto.numberPhone()) && repository.existsByNumberPhone(dto.numberPhone())) {
            throw new NumberPhoneAlreadyExistsException("Данный номер телефона занят");
        }

        entity.setFirstName(dto.firstName());
        entity.setLastName(dto.lastName());
        entity.setPatronymic(dto.patronymic());
        entity.setEmail(dto.email());
        entity.setNumberPhone(dto.numberPhone());
        entity.setDepartment(dto.department());
        entity.setPost(dto.post());

        return ResponseEmployeeDTO.from(repository.save(entity));
    }
}
