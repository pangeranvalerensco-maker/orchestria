package com.pangeranvalerensco.orchestria.organization_service.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse<Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden: Anda tidak memiliki akses ke endpoint ini",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Object>> handleGeneralError(
            Exception ex,
            HttpServletRequest request) {
        ex.printStackTrace();
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Terjadi Kesalahan Pada server",
                request.getRequestURI(),
                null);
    }

    // ── Archive module exception handlers ───────────────────────────────────

    @ExceptionHandler(ArchiveDocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse<Object>> handleArchiveDocumentNotFound(
            ArchiveDocumentNotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(ArchiveInvalidFileException.class)
    public ResponseEntity<ErrorResponse<Object>> handleArchiveInvalidFile(
            ArchiveInvalidFileException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(ArchiveFileTooLargeException.class)
    public ResponseEntity<ErrorResponse<Object>> handleArchiveFileTooLarge(
            ArchiveFileTooLargeException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse<Object>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Ukuran file melebihi batas maksimum yang dikonfigurasi",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(ArchiveUnsupportedMediaTypeException.class)
    public ResponseEntity<ErrorResponse<Object>> handleArchiveUnsupportedMediaType(
            ArchiveUnsupportedMediaTypeException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ex.getMessage(),
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(ArchiveStorageException.class)
    public ResponseEntity<ErrorResponse<Object>> handleArchiveStorageError(
            ArchiveStorageException ex,
            HttpServletRequest request) {
        // Jangan kembalikan physical path kepada client
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Gagal memproses file arsip. Silakan hubungi administrator.",
                request.getRequestURI(),
                null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse<Object>> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Konflik data: dokumen telah dimodifikasi oleh pengguna lain. Silakan coba lagi.",
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
