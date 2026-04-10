package com.example.employee.entity;

import com.example.employee.dto.RequestEmployeeDTO;
import com.example.employee.enums.Department;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "employee")
@Builder
@AllArgsConstructor
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="first_name")
    private String firstName;

    @Column(name ="last_name")
    private String lastName;

    private String patronymic;

    @Enumerated(EnumType.STRING)
    private Department department;

    @Column(name = "number_phone", unique = true)
    private String numberPhone;

    @Column(unique = true)
    private String email;

    private String post;

    public static EmployeeEntity from(RequestEmployeeDTO dto) {
        return EmployeeEntity.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .patronymic(dto.patronymic())
                .department(dto.department())
                .numberPhone(dto.numberPhone())
                .email(dto.email())
                .post(dto.post())
                .build();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> objectEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != objectEffectiveClass) {
            return false;
        }
        EmployeeEntity that = (EmployeeEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
