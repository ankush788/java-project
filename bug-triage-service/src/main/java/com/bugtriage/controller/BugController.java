package com.bugtriage.controller;

import com.bugtriage.dto.CreateBugRequest;
import com.bugtriage.dto.BugResponse;
import com.bugtriage.dto.PageResponse;
import com.bugtriage.dto.UpdateBugRequest;
import com.bugtriage.service.BugService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bugs")
public class BugController {

    private static final Logger log = LoggerFactory.getLogger(BugController.class);

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBug(@RequestHeader("X-Correlation-ID") String correlationId,
                                                         @Valid @RequestBody CreateBugRequest request) {
        log.info("correlationId: {} - POST /api/bugs - Creating new bug", correlationId);
        BugResponse response = bugService.createBug(correlationId, request);
        return new ResponseEntity<>(withCorrelationId(correlationId, response), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBugs(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("correlationId: {} - GET /api/bugs - Fetching bugs with page: {}, size: {}", correlationId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<BugResponse> response = bugService.getAllBugs(correlationId, pageable);
        return new ResponseEntity<>(withCorrelationId(correlationId, response), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBugById(@RequestHeader("X-Correlation-ID") String correlationId,
                                                          @PathVariable Long id) {
        log.info("correlationId: {} - GET /api/bugs/{} - Fetching bug", correlationId, id);
        BugResponse response = bugService.getBugById(correlationId, id);
        return new ResponseEntity<>(withCorrelationId(correlationId, response), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBug(
            @RequestHeader("X-Correlation-ID") String correlationId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBugRequest request) {
        log.info("correlationId: {} - PUT /api/bugs/{} - Updating bug", correlationId, id);
        BugResponse response = bugService.updateBug(correlationId, id, request);
        return new ResponseEntity<>(withCorrelationId(correlationId, response), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBug(@RequestHeader("X-Correlation-ID") String correlationId,
                                                         @PathVariable Long id) {
        log.info("correlationId: {} - DELETE /api/bugs/{} - Deleting bug", correlationId, id);
        bugService.deleteBug(correlationId, id);

        return new ResponseEntity<>(withCorrelationId(correlationId, Map.of("message", "Bug deleted successfully")), HttpStatus.OK);
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.HEAD})
    public void handleUnknownBugEndpoint() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API not exist");
    }

    private Map<String, Object> withCorrelationId(String correlationId, Object response) {
        Map<String, Object> wrappedResponse = new LinkedHashMap<>();
        wrappedResponse.put("correlationId", correlationId);
        wrappedResponse.put("data", response);
        return wrappedResponse;
    }
}
