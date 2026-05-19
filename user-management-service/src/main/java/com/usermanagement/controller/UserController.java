package com.usermanagement.controller;

import com.usermanagement.dto.UpdateUserRequest;
import com.usermanagement.dto.UserResponse;
import com.usermanagement.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users (pagination)
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        log.info(
                "correlationId: {} - GET /api/users - Fetching users with page: {}, size: {}",
                correlationId,
                page,
                size
        );

        Page<UserResponse> response =
                userService.getAllUsers(correlationId, page, size);

        return ResponseEntity.ok(response);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @PathVariable @Min(1) Long id
    ) {

        log.info(
                "correlationId: {} - GET /api/users/{} - Fetching user",
                correlationId,
                id
        );

        UserResponse response =
                userService.getUserById(correlationId, id);

        return ResponseEntity.ok(response);
    }

    // Get user by email
    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @RequestParam @Email String email
    ) {

        log.info(
                "correlationId: {} - GET /api/users/email - Fetching user by email",
                correlationId
        );

        UserResponse response =
                userService.getUserByEmail(correlationId, email);

        return ResponseEntity.ok(response);
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        log.info(
                "correlationId: {} - PUT /api/users/{} - Updating user",
                correlationId,
                id
        );

        UserResponse response =
                userService.updateUser(correlationId, id, request);

        return ResponseEntity.ok(response);
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @PathVariable @Min(1) Long id
    ) {

        log.info(
                "correlationId: {} - DELETE /api/users/{} - Deleting user",
                correlationId,
                id
        );

        userService.deleteUser(correlationId, id);

        return ResponseEntity.ok(
                Map.of("message", "User deleted successfully")
        );
    }

}