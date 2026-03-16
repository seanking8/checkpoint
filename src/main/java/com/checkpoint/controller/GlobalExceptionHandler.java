package com.checkpoint.controller;

import com.checkpoint.error.ApiErrorResponse;
import com.checkpoint.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

// Centralised error handling for all REST controllers
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(DomainException ex) {
        HttpStatus status = ex.getErrorCode().getStatus();
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(status).body(body);
    }

    // 400: Bean Validation failures (@NotBlank, @Size, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        ApiErrorResponse body = new ApiErrorResponse(
                400,
                "VALIDATION_FAILED",
                "Validation failed",
                errors
        );
        return ResponseEntity.badRequest().body(body);
    }

    // 401: Wrong username or password during login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                401,
                "INVALID_CREDENTIALS",
                "Invalid username or password",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
}

