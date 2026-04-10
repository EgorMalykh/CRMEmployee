package com.example.employee.exception;

public class NumberPhoneAlreadyExistsException extends RuntimeException {
    public NumberPhoneAlreadyExistsException(String message) {
        super(message);
    }
}
