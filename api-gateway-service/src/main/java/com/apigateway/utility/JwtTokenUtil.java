package com.apigateway.utility;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:mySuperSecretKeyForJwtTokenValidationInApiGatewayServiceWithMinimum256Bits}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long tokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId, Map<String, Object> claims) {
        if (claims == null) {
            claims = new HashMap<>();
        }
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String userId) {
        return generateToken(userId, new HashMap<>());
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT signature", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new IllegalArgumentException("JWT claims string is empty", e);
        }
    }

    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header format");
    }

    public String extractUserId(String token) {
        return validateToken(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getTokenType(String token) {
        Claims claims = validateToken(token);
        return (String) claims.get("type");
    }
}
