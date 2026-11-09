package dev.devon.clinic.api;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.method.ParameterErrors;
import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class ApiErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> domain(ApiException e, HttpServletRequest request) {
        return error(e.getStatus(), e.getCode(), e.getMessage(), request, Map.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, fields);
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> parameters(HandlerMethodValidationException e, HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        e.getParameterValidationResults().forEach(result -> {
            if (result instanceof ParameterErrors errors) {
                errors.getFieldErrors().forEach(field -> fields.putIfAbsent(field.getField(), field.getDefaultMessage()));
            } else {
                fields.put(Objects.requireNonNullElse(result.getMethodParameter().getParameterName(), "parameter"),
                    result.getResolvableErrors().isEmpty() ? "Invalid value" : result.getResolvableErrors().get(0).getDefaultMessage());
            }
        });
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", request, fields);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> malformed(Exception e, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Malformed JSON, date, or parameter", request, Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException e, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "RELATIONSHIP_CONFLICT", "Operation conflicts with related records", request, Map.of());
    }
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> missing(NoResourceFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Endpoint not found", request, Map.of());
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> method(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Method not allowed", request, Map.of());
    }
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> media(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Use application/json", request, Map.of());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        log.error("Unexpected request failure at {}", request.getRequestURI(), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", request, Map.of());
    }
    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
        HttpServletRequest request, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message, request.getRequestURI(), fields));
    }
}
