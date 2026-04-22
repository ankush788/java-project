package com.usermanagement.controller;

import com.usermanagement.dto.UpdateUserRequest;
import com.usermanagement.dto.UserResponse;
import com.usermanagement.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users (pagination)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("correlationId: {} - GET /api/users - Fetching users with page: {}, size: {}", correlationId, page, size);
        return ResponseEntity.ok(withCorrelationId(correlationId, userService.getAllUsers(correlationId, page, size)));
    }

    // Get user by ID (primary way)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@RequestHeader("X-Correlation-ID") String correlationId,
                                                           @PathVariable @Min(1) Long id) {
        log.info("correlationId: {} - GET /api/users/{} - Fetching user", correlationId, id);
        return ResponseEntity.ok(withCorrelationId(correlationId, userService.getUserById(correlationId, id)));
    }

    // Get user by email (separate endpoint to avoid conflict)
    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@RequestHeader("X-Correlation-ID") String correlationId,
                                                              @RequestParam @Email String email) {
        log.info("correlationId: {} - GET /api/users/email - Fetching user by email", correlationId);
        return ResponseEntity.ok(withCorrelationId(correlationId, userService.getUserByEmail(correlationId, email)));
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("correlationId: {} - PUT /api/users/{} - Updating user", correlationId, id);
        return ResponseEntity.ok(withCorrelationId(correlationId, userService.updateUser(correlationId, id, request)));
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestHeader("X-Correlation-ID") String correlationId,
                                                          @PathVariable @Min(1) Long id) {
        log.info("correlationId: {} - DELETE /api/users/{} - Deleting user", correlationId, id);
        userService.deleteUser(correlationId, id);
        return ResponseEntity.ok(withCorrelationId(correlationId, Map.of("message", "User deleted successfully")));
    }

    private Map<String, Object> withCorrelationId(String correlationId, Object response) {
        Map<String, Object> wrappedResponse = new LinkedHashMap<>();
        wrappedResponse.put("correlationId", correlationId);
        wrappedResponse.put("data", response);
        return wrappedResponse;
    }

}
