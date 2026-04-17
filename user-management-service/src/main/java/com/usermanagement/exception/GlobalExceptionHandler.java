package com.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Handles ResponseStatusException (thrown manually in service layer)
     * Example:
     *   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
     * Response:
     *   400 Bad Request with message "Email already exists"
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex) {
        return buildErrorResponse(ex.getStatusCode(), ex.getReason());
    }

    /*
     * Handles validation errors for @Valid @RequestBody (DTO validation)
     * Example:
     *   DTO has @NotBlank email, @Size(min=6) password
     *   Request: { "email": "", "password": "123" }
     * Response:
     *   400 Bad Request with message:
     *   "email must not be blank; password size must be at least 6"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        String message = String.join("; ", fieldErrors);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /*
     * Handles type mismatch errors (wrong data type in path/query params)
     * Example:
     *   API: GET /users/{id} where id is Long
     *   Request: /users/abc
     * Response:
     *   400 Bad Request with message:
     *   "Invalid value 'abc' for parameter 'id'. Expected type: Long"
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /*
     * Handles validation errors on @PathVariable and @RequestParam
     * (requires @Validated on controller)
     * Example:
     *   @PathVariable @Min(1) Long id
     *   Request: /users/0
     * Response:
     *   400 Bad Request with message:
     *   "getUser.id must be greater than or equal to 1"
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining("; "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /*
     * Handles all unhandled exceptions (fallback)
     * Example:
     *   NullPointerException, ArithmeticException, etc.
     * Response:
     *   500 Internal Server Error with generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatusCode status, String message) {
        String reason = (status instanceof HttpStatus httpStatus)
                ? httpStatus.getReasonPhrase()
                : status.toString();

        ApiError error = new ApiError(
                status.value(),
                reason,
                message == null || message.isBlank() ? reason : message,
                Instant.now().toString()
        );
        return ResponseEntity.status(status).body(error);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }

    public record ApiError(int status, String error, String message, String timestamp) {}
}