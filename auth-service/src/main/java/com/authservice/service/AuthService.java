package com.authservice.service;

import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.config.JwtProperties;
import com.authservice.utility.PasswordEncoderUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserRepository userRepository,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }

    public TokenResponse register(String correlationId, RegisterRequest request) {
        log.info("correlationId: {} - Registering user with email: {}", correlationId, request.email());

        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            log.warn("correlationId: {} - Email already registered: {}", correlationId, request.email());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already registered");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(PasswordEncoderUtil.encodePassword(request.password())); // BCrypt encoded
        userRepository.save(user);
        log.info("correlationId: {} - User registered successfully with id: {}", correlationId, user.getId());

        return createTokenResponse(correlationId, user);
    }

    public TokenResponse login(String correlationId, LoginRequest request) {
        log.info("correlationId: {} - Login attempt for email: {}", correlationId, request.email());

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> {
                    log.warn("correlationId: {} - Login failed, user not found for email: {}", correlationId, request.email());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!PasswordEncoderUtil.verifyPassword(request.password(), user.getPassword())) { // BCrypt verification
            log.warn("correlationId: {} - Login failed, invalid password for email: {}", correlationId, request.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("correlationId: {} - Login successful for user id: {}", correlationId, user.getId());
        return createTokenResponse(correlationId, user);
    }

    private TokenResponse createTokenResponse(String correlationId, User user) {
        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId())) //sub (subject): the unique identifier of the  the token represents (ex userId --> user token).
                .claim("userEmail", user.getEmail()) // claim: additional key-value data that provides extra information about the token object context. 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.accessTokenValidity()))
                .signWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes()), SignatureAlgorithm.HS256)
                .compact();

        log.info("correlationId: {} - Token created for user id: {}", correlationId, user.getId());
        return new TokenResponse(token, jwtProperties.accessTokenValidity());
    }
}
