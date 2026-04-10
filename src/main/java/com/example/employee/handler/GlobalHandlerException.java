package com.example.employee.handler;

import com.example.employee.exception.EmailAlreadyExistsException;
import com.example.employee.exception.EmployeeNotFoundException;
import com.example.employee.exception.NumberPhoneAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(NumberPhoneAlreadyExistsException.class)
    public ResponseEntity<ResponseError> handlerNumberPhoneExisted(
            NumberPhoneAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseError.builder()
                        .message(ex.getMessage())
                        .field("numberPhone")
                        .path(request.getRequestURI())
                        .errorCode("PHONE_EXISTS")
                        .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                        .build()
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseError> handlerEmailExisted(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseError.builder()
                        .message(ex.getMessage())
                        .field("email")
                        .path(request.getRequestURI())
                        .errorCode("EMAIL_EXISTS")
                        .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                        .build()
        );
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ResponseError> handlerEmployeeNotFound(
            EmployeeNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseError.builder()
                        .message(ex.getMessage())
                        .field("id")
                        .path(request.getRequestURI())
                        .errorCode("EMPLOYEE_NOT_FOUND")
                        .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseError> handlerMethodArgumentInvalid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        var fieldError = ex.getFieldError();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseError.builder()
                        .message(fieldError.getDefaultMessage())
                        .field(fieldError.getField())
                        .path(request.getRequestURI())
                        .errorCode("VALIDATION_ERROR")
                        .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                        .build()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseError> handleDataIntegrityViolation(
            DataIntegrityViolationException e, HttpServletRequest request) {
        String rootMsg = e.getRootCause().getMessage().toLowerCase();

        String field = rootMsg.contains("email") ? "email" :
                rootMsg.contains("phone") ? "numberPhone" : "database";

        String code = field.equals("email") ? "EMAIL_EXISTS" :
                field.equals("numberPhone") ? "PHONE_EXISTS" : "DATA_INTEGRITY_VIOLATION";

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ResponseError.builder()
                        .message("Нарушение уникальности: " + field)
                        .field(field)
                        .path(request.getRequestURI())
                        .errorCode(code)
                        .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseError.builder()
                        .message("Неверный тип параметра")
                        .field(ex.getName())
                        .path(request.getRequestURI())
                        .errorCode("TYPE_MISMATCH")
                        .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                        .build()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        var violation = ex.getConstraintViolations().iterator().next();
        String field = violation.getPropertyPath().toString();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ResponseError.builder()
                        .message(violation.getMessage())
                        .field(field)
                        .path(request.getRequestURI())
                        .errorCode("CONSTRAINT_VIOLATION")
                        .timestamp(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                        .build()
        );
    }
}