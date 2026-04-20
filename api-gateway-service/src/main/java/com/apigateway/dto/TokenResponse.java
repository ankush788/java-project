package com.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {
    private String token;
    private String userId;
    private Long expiresIn;
    private String tokenType;
}
