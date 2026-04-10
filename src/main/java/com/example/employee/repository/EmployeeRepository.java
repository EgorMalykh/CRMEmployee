package com.example.employee.repository;

import com.example.employee.entity.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    Page<EmployeeEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrPatronymicContainingIgnoreCase(
            String firstName, String lastName, String patronymic, Pageable pageable);

    Page<EmployeeEntity> findAll(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByNumberPhone(String numberPhone);
}
