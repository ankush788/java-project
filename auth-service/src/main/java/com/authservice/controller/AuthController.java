package com.authservice.controller;

import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestHeader("X-Correlation-ID") String correlationId,
                                                  @Valid @RequestBody RegisterRequest request) {
        log.info("correlationId: {} - POST /api/auth/register - Registering user", correlationId);
        TokenResponse response = authService.register(correlationId, request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestHeader("X-Correlation-ID") String correlationId,
                                               @Valid @RequestBody LoginRequest request) {
        log.info("correlationId: {} - POST /api/auth/login - Logging in user", correlationId);
        
          TokenResponse response =  authService.login(correlationId, request);
         return ResponseEntity.ok().body(response);
    }

}
