package com.authservice.dto;

public record TokenResponse(
    String tokenType,
    String accessToken,
    long expiresIn
) {

    public TokenResponse(String accessToken, long expiresIn) {
        this("Bearer", accessToken, expiresIn);
    }

    public TokenResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
}
