package com.reliaquest.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class EmployeeExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Error> handleException(EmployeeNotFoundException e, HttpServletRequest request) {
        Error apiError = new Error(
                request.getRequestURI(),
                Collections.singletonList(e.getMessage()),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now());
        log.info(e.getMessage());

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmployeeException.class)
    public ResponseEntity<Error> handleException(EmployeeException e, HttpServletRequest request) {
        Error apiError = new Error(
                request.getRequestURI(),
                Collections.singletonList(e.getMessage()),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now());
        log.info(e.getMessage());

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleException(MethodArgumentNotValidException e, HttpServletRequest request) {
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .toList();

        Error apiError =
                new Error(request.getRequestURI(), errorMessages, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

        log.info(String.valueOf(errorMessages));

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }
}
