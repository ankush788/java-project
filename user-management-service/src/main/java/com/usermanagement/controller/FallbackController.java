package com.usermanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

// it for all  url which is invalid
@RestController
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.PATCH,
            RequestMethod.OPTIONS,
            RequestMethod.HEAD
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleUnknownEndpoint(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        log.warn("correlationId: {} - Unknown user management API endpoint requested", correlationId);

        HttpHeaders headers = new HttpHeaders();
        if (correlationId != null && !correlationId.isBlank()) {
            headers.set("X-Correlation-ID", correlationId);
        }

        return new ResponseEntity<>(
                Map.of(
                        "status", HttpStatus.NOT_FOUND.value(),
                        "error", HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "message", "API not exist",
                        "timestamp", Instant.now().toString()
                ),
                headers,
                NOT_FOUND
        );
    }
}
