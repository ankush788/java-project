package com.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)  // it excute this exception handler first then other expection handler and even spring expection handler of microservice in which "commmon" dependency is used
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Handles manually thrown ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {

        String correlationId = getCorrelationId(request);

        log.warn(
                "ResponseStatusException | correlationId={} | status={} | message={} | path={}",
                correlationId,
                ex.getStatusCode(),
                ex.getReason(),
                request.getRequestURI()
        );

        return buildErrorResponse(
                ex.getStatusCode(),
                ex.getReason(),
                request
        );
    }

    /*
     * Handles @Valid DTO validation failures
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String correlationId = getCorrelationId(request);

        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        String message = String.join("; ", fieldErrors);

        log.warn(
                "Validation failed | correlationId={} | message={} | path={}",
                correlationId,
                message,
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    /*
     * Handles invalid path/query parameter types
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        String correlationId = getCorrelationId(request);

        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "Unknown";

        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                expectedType
        );

        log.warn(
                "MethodArgumentTypeMismatchException | correlationId={} | message={} | path={}",
                correlationId,
                message,
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    /*
     * Handles @Validated validation failures
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        String correlationId = getCorrelationId(request);

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));

        log.warn(
                "ConstraintViolationException | correlationId={} | message={} | path={}",
                correlationId,
                message,
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }

    /*
     * Fallback handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(
            Exception ex,
            HttpServletRequest request
    ) {

        String correlationId = getCorrelationId(request);

        log.error(
                "Unhandled exception | correlationId={} | path={}",
                correlationId,
                request.getRequestURI(),
                ex
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            HttpStatusCode status,
            String message,
            HttpServletRequest request
    ) {

        String reason = (status instanceof HttpStatus httpStatus)
                ? httpStatus.getReasonPhrase()
                : status.toString();

        ApiError error = new ApiError(
                status.value(),
                reason,
                message == null || message.isBlank() ? reason : message,
                Instant.now().toString()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }


        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiError> handleNoResourceFoundException(
                NoResourceFoundException ex,
                HttpServletRequest request
        ) {

        String correlationId = getCorrelationId(request);

        log.warn(
                "NoResourceFoundException | correlationId={} | path={}",
                correlationId,
                request.getRequestURI()
        );

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "API not exist",
                request
        );
        }
    private String getCorrelationId(HttpServletRequest request) {

        String correlationId = request.getHeader("correlation_id");

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = request.getHeader("X-Correlation-ID");
        }

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = request.getHeader("X-Correlation-Id");
        }

        return (correlationId == null || correlationId.isBlank())
                ? "N/A"
                : correlationId;
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }

    public record ApiError(
            int status,
            String error,
            String message,
            String timestamp
    ) {}
}