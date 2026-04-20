package com.apigateway.controller;

import com.apigateway.utility.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private final JwtTokenUtil jwtTokenUtil;

    public GatewayController(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "API Gateway Service");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenUtil.extractToken(authHeader);
            var claims = jwtTokenUtil.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", claims.getSubject());
            response.put("issuedAt", claims.getIssuedAt());
            response.put("expiresAt", claims.getExpiration());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating token", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}
