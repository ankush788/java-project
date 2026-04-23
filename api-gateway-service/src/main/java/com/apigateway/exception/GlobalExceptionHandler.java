package com.apigateway.exception;

import org.springframework.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        log.error("Response Status Exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getStatusCode(), ex.getReason(), exchange);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, ServerWebExchange exchange) {
        log.error("Validation Exception: {}", ex.getMessage());
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        String message = String.join("; ", fieldErrors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, ServerWebExchange exchange) {
        log.error("Type Mismatch Exception: {}", ex.getMessage());
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, ServerWebExchange exchange) {
        log.error("Constraint Violation Exception: {}", ex.getMessage());
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, exchange);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected Exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", exchange);
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatusCode status, String message, ServerWebExchange exchange) {
        String reason = (status instanceof HttpStatus httpStatus)
                ? httpStatus.getReasonPhrase()
                : status.toString();

        ApiError error = new ApiError(
                status.value(),
                reason,
                message == null || message.isBlank() ? reason : message,
                Instant.now().toString()
        );

        HttpHeaders headers = new HttpHeaders();
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId != null && !correlationId.isBlank()) {
            headers.set(CORRELATION_ID_HEADER, correlationId);
        }

        return new ResponseEntity<>(error, headers, status);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }

    public record ApiError(int status, String error, String message, String timestamp) {}
}
