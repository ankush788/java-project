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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/bugs")
public class BugController {

    private static final Logger log = LoggerFactory.getLogger(BugController.class);

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @PostMapping
    public ResponseEntity<BugResponse> createBug(@Valid @RequestBody CreateBugRequest request) {
        log.info("POST /api/v1/bugs - Creating new bug");
        BugResponse response = bugService.createBug(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageResponse<BugResponse>> getAllBugs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/bugs - Fetching bugs with page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        PageResponse<BugResponse> response = bugService.getAllBugs(pageable);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BugResponse> getBugById(@PathVariable Long id) {
        log.info("GET /api/v1/bugs/{} - Fetching bug", id);
        BugResponse response = bugService.getBugById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BugResponse> updateBug(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBugRequest request) {
        log.info("PUT /api/v1/bugs/{} - Updating bug", id);
        BugResponse response = bugService.updateBug(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBug(@PathVariable Long id) {
        log.info("DELETE /api/v1/bugs/{} - Deleting bug", id);
        bugService.deleteBug(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS, RequestMethod.HEAD})
    public void handleUnknownBugEndpoint() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API not exist");
    }
}
