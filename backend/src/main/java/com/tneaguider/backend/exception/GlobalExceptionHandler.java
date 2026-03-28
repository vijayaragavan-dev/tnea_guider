package com.tneaguider.backend.exception;

import com.tneaguider.backend.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException exception,
                                                              HttpServletRequest request) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(NoResultsFoundException.class)
    public ResponseEntity<ApiError> handleNoResults(NoResultsFoundException exception, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception,
                                                           HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidJson(HttpMessageNotReadableException exception,
                                                      HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST,
                "Invalid request body. Verify JSON field types and formats.",
                request.getRequestURI());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccess(DataAccessException exception, HttpServletRequest request) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE,
                "Database connection issue. Please try again later.",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception exception, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error occurred.",
                request.getRequestURI());
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, String path) {
        ApiError body = new ApiError();
        body.setTimestamp(LocalDateTime.now());
        body.setStatus(status.value());
        body.setError(status.getReasonPhrase());
        body.setMessage(message);
        body.setPath(path);
        return ResponseEntity.status(status).body(body);
    }
}
