package com.authservice.controller;

import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestHeader("X-Correlation-ID") String correlationId,
                                                  @Valid @RequestBody RegisterRequest request) {
        log.info("correlationId: {} - POST /api/auth/register - Registering user", correlationId);
        return withCorrelationIdHeader(correlationId, authService.register(correlationId, request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestHeader("X-Correlation-ID") String correlationId,
                                               @Valid @RequestBody LoginRequest request) {
        log.info("correlationId: {} - POST /api/auth/login - Logging in user", correlationId);
        return withCorrelationIdHeader(correlationId, authService.login(correlationId, request));
    }

    private <T> ResponseEntity<T> withCorrelationIdHeader(String correlationId, T response) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-ID", correlationId);
        return ResponseEntity.ok().headers(headers).body(response);
    }

}
