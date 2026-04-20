package com.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitInfo {
    private long remainingRequests;
    private long capacity;
    private long refillRate;
    private long windowSeconds;
}
