package com.bugtriage.dto;

import com.bugtriage.entity.BugSeverity;
import com.bugtriage.entity.BugStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record BugResponse(
    Long id,
    String title,
    String description,
    BugStatus status,
    BugSeverity severity,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {}
