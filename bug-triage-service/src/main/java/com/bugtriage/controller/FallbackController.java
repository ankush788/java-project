package com.bugtriage.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestController
public class FallbackController {

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.PATCH,
            RequestMethod.OPTIONS,
            RequestMethod.HEAD
    })
    public ResponseEntity<Map<String, Object>> handleUnknownEndpoint(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        log.warn("correlationId: {} - Unknown bug triage API endpoint requested", correlationId);

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
