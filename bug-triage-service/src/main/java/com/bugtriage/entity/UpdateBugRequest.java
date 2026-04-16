package com.bugtriage.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBugRequest(
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    String title,

    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    String description,

    @NotNull(message = "Status is required")
    BugStatus status,

    @NotNull(message = "Severity is required")
    BugSeverity severity
) {}
