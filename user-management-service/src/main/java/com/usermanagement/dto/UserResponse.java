package com.usermanagement.dto;

import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    Instant createdAt,
    Instant updatedAt
) {}