package com.reliaquest.api.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String input) {
        super("Unable to find employee with id or name: " + input);
    }
}
