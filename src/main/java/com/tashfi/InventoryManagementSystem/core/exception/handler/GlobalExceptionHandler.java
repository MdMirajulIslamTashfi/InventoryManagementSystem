package com.tashfi.InventoryManagementSystem.core.exception.handler;

import com.tashfi.InventoryManagementSystem.core.exception.*;
import com.tashfi.InventoryManagementSystem.core.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public Mono<ErrorResponse> handleValidation(ValidationException ex) {
        return Mono.just(ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public Mono<ErrorResponse> handleDuplicate(DuplicateEmailException ex) {
        return Mono.just(ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Entry")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ErrorResponse> handleNotFound(CustomerNotFoundException ex) {
        return Mono.just(ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        return Mono.just(ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public Mono<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
        return Mono.just(ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(DuplicateProductException.class)
    public Mono<ErrorResponse> handleDuplicateProduct(DuplicateProductException ex) {
        return Mono.just(ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Duplicate Entry")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public Mono<ErrorResponse> handleGeneric(Exception ex) {
        ex.printStackTrace();
        return Mono.just(ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build());
    }
}