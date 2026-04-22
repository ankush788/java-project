package com.bugtriage.service;

import com.bugtriage.cache.CacheManager;
import com.bugtriage.dto.CreateBugRequest;
import com.bugtriage.dto.BugResponse;
import com.bugtriage.dto.PageResponse;
import com.bugtriage.dto.UpdateBugRequest;
import com.bugtriage.entity.Bug;
import com.bugtriage.entity.BugStatus;
import com.bugtriage.repository.BugRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


/**
 * Bug Service with Redis cache-aside pattern implementation
 * 
 * Caching Strategy:
 * - getBugById: Uses cache-aside pattern (check cache first, then DB if miss)
 * - getAllBugs: No caching (frequently changes due to CRUD operations)
 * - createBug: No caching (new entities)
 * - updateBug: Invalidates cache after update
 * - deleteBug: Invalidates cache after delete
 */
@Service
@Transactional
public class BugService {

    private static final Logger log = LoggerFactory.getLogger(BugService.class);

    private final BugRepository bugRepository;
    private final CacheManager cacheManager;

    public BugService(BugRepository bugRepository, CacheManager cacheManager) {
        this.bugRepository = bugRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Create a new bug
     * Cache immediately after creation for potential immediate access
     *
     * @param request the create bug request
     * @return the created bug response
     */
    public BugResponse createBug(String correlationId, CreateBugRequest request) {
        log.info("correlationId: {} - Creating new bug with title: {}", correlationId, request.title());

        Bug bug = Bug.builder()
            .title(request.title())
            .description(request.description())
            .severity(request.severity())
            .status(BugStatus.OPEN)
            .build();

        Bug saved = bugRepository.save(bug);
        log.info("correlationId: {} - Bug created successfully with id: {}", correlationId, saved.getId());

        BugResponse response = mapToResponse(saved);

        // Cache the newly created bug for potential immediate access
        cacheManager.cacheBug(correlationId, response.id(), response);

        return response;
    }

    /**
     * Get all bugs with pagination
     * NOT cached as this endpoint frequently changes due to CRUD operations
     * 
     * @param pageable pagination information
     * @return paginated bug responses
     */
    @Transactional(readOnly = true)
    public PageResponse<BugResponse> getAllBugs(String correlationId, Pageable pageable) {
        log.info("correlationId: {} - Fetching bugs with page: {}, size: {}", correlationId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Bug> page = bugRepository.findAll(pageable);

        PageResponse<BugResponse> response = new PageResponse<>(
            page.getContent().stream().map(this::mapToResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );

        log.info("correlationId: {} - Fetched {} bugs", correlationId, page.getContent().size());
        return response;
    }

    /**
     * Get bug by ID using cache-aside pattern
     * 
     * Cache-aside flow:
     * 1. Try to get from cache (Redis)
     * 2. If cache miss, fetch from database
     * 3. Store in cache with TTL
     * 4. Return the data
     * 
     * @param id the bug ID
     * @return the bug response
     * @throws ResourceNotFoundException if bug not found
     */
    @Transactional(readOnly = true)
    public BugResponse getBugById(String correlationId, Long id) {
        log.info("correlationId: {} - Fetching bug with id: {}", correlationId, id);

        // Step 1: Try to get from cache
        BugResponse cachedResponse = cacheManager.getCachedBug(correlationId, id, BugResponse.class);
        if (cachedResponse != null) {
            log.info("correlationId: {} - Bug retrieved from cache - id: {}", correlationId, id);
            return new BugResponse(
                cachedResponse.id(),
                cachedResponse.title(),
                cachedResponse.description(),
                cachedResponse.status(),
                cachedResponse.severity(),
                cachedResponse.createdAt(),
                cachedResponse.updatedAt()
            );
        }

        // Step 2: Cache miss - fetch from database
        Bug bug = bugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("correlationId: {} - Bug not found with id: {}", correlationId, id);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Bug not found with id: " + id);
            });

        // Step 3: Convert to response
        BugResponse response = mapToResponse(bug);

        // Step 4: Store in cache
        cacheManager.cacheBug(correlationId, id, response);
        log.info("correlationId: {} - Bug cached after database fetch - id: {}", correlationId, id);

        return response;
    }

    /**
     * Update an existing bug and invalidate cache
     * 
     * @param id the bug ID
     * @param request the update bug request
     * @return the updated bug response
     * @throws ResourceNotFoundException if bug not found
     */
    public BugResponse updateBug(String correlationId, Long id, UpdateBugRequest request) {
        log.info("correlationId: {} - Updating bug with id: {}", correlationId, id);

        Bug bug = bugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("correlationId: {} - Bug not found with id: {}", correlationId, id);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Bug not found with id: " + id);
            });

        if (request.title() != null) {
            bug.setTitle(request.title());
        }
        if (request.description() != null) {
            bug.setDescription(request.description());
        }
        if (request.status() != null) {
            bug.setStatus(request.status());
        }
        if (request.severity() != null) {
            bug.setSeverity(request.severity());
        }

        Bug updated = bugRepository.save(bug);
        log.info("correlationId: {} - Bug updated successfully with id: {}", correlationId, id);

        // Invalidate cache after update
        cacheManager.invalidateBugCache(correlationId, id);
        log.info("correlationId: {} - Cache invalidated for updated bug - id: {}", correlationId, id);

        return mapToResponse(updated);
    }

    /**
     * Delete a bug and invalidate cache
     * 
     * @param id the bug ID
     * @throws ResourceNotFoundException if bug not found
     */
    public void deleteBug(String correlationId, Long id) {
        log.info("correlationId: {} - Deleting bug with id: {}", correlationId, id);

        Bug bug = bugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("correlationId: {} - Bug not found with id: {}", correlationId, id);
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Bug not found with id: " + id);
            });

        bugRepository.delete(bug);
        log.info("correlationId: {} - Bug deleted successfully with id: {}", correlationId, id);

        // Invalidate cache after delete
        cacheManager.invalidateBugCache(correlationId, id);
        log.info("correlationId: {} - Cache invalidated for deleted bug - id: {}", correlationId, id);
    }

    /**
     * Map Bug entity to BugResponse DTO
     * 
     * @param bug the bug entity
     * @return the bug response DTO
     */
    private BugResponse mapToResponse(Bug bug) {
        return new BugResponse(
            bug.getId(),
            bug.getTitle(),
            bug.getDescription(),
            bug.getStatus(),
            bug.getSeverity(),
            bug.getCreatedAt(),
            bug.getUpdatedAt()
        );
    }
}
