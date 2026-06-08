package com.pangeranvalerensco.orchestria.organization_service.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pangeranvalerensco.orchestria.organization_service.payload.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse<Object>> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<Object>> handleValidationError(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validasi Data Gagal",
                request.getRequestURI(),
                errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse<Object>> handleUndreadableJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Request Body tidak valid atau format JSON salah",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Object>> handleGeneralError(
            Exception ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Terjadi Kesalahan Pada server",
                request.getRequestURI(),
                null);
    }

    private ResponseEntity<ErrorResponse<Object>> buildErrorResponse(
            HttpStatus status,
            String message,
            String path,
            Object details) {
        ErrorResponse<Object> response = ErrorResponse.builder()
                .success(false)
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
