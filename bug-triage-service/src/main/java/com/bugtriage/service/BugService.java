package com.bugtriage.service;

import com.bugtriage.cache.CacheManager;
import com.bugtriage.dto.CreateBugRequest;
import com.bugtriage.dto.BugResponse;
import com.bugtriage.dto.PageResponse;
import com.bugtriage.dto.UpdateBugRequest;
import com.bugtriage.entity.Bug;
import com.bugtriage.entity.BugStatus;
import com.bugtriage.exception.ResourceNotFoundException;
import com.bugtriage.repository.BugRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public BugResponse createBug(CreateBugRequest request) {
        log.info("Creating new bug with title: {}", request.title());

        Bug bug = Bug.builder()
            .title(request.title())
            .description(request.description())
            .severity(request.severity())
            .status(BugStatus.OPEN)
            .build();

        Bug saved = bugRepository.save(bug);
        log.info("Bug created successfully with id: {}", saved.getId());

        BugResponse response = mapToResponse(saved);

        // Cache the newly created bug for potential immediate access
        cacheManager.cacheBug(response.id(), response);

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
    public PageResponse<BugResponse> getAllBugs(Pageable pageable) {
        log.info("Fetching bugs with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Bug> page = bugRepository.findAll(pageable);

        PageResponse<BugResponse> response = new PageResponse<>(
            page.getContent().stream().map(this::mapToResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );

        log.info("Fetched {} bugs", page.getContent().size());
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
    public BugResponse getBugById(Long id) {
        log.info("Fetching bug with id: {}", id);

        // Step 1: Try to get from cache
        BugResponse cachedResponse = cacheManager.getCachedBug(id, BugResponse.class);
        if (cachedResponse != null) {
            log.info("Bug retrieved from cache - id: {}", id);
            return cachedResponse;
        }

        // Step 2: Cache miss - fetch from database
        Bug bug = bugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Bug not found with id: {}", id);
                return new ResourceNotFoundException("Bug not found with id: " + id);
            });

        // Step 3: Convert to response
        BugResponse response = mapToResponse(bug);

        // Step 4: Store in cache
        cacheManager.cacheBug(id, response);
        log.info("Bug cached after database fetch - id: {}", id);

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
    public BugResponse updateBug(Long id, UpdateBugRequest request) {
        log.info("Updating bug with id: {}", id);

        Bug bug = bugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Bug not found with id: {}", id);
                return new ResourceNotFoundException("Bug not found with id: " + id);
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
        log.info("Bug updated successfully with id: {}", id);

        // Invalidate cache after update
        cacheManager.invalidateBugCache(id);
        log.info("Cache invalidated for updated bug - id: {}", id);

        return mapToResponse(updated);
    }

    /**
     * Delete a bug and invalidate cache
     * 
     * @param id the bug ID
     * @throws ResourceNotFoundException if bug not found
     */
    public void deleteBug(Long id) {
        log.info("Deleting bug with id: {}", id);

        Bug bug = bugRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Bug not found with id: {}", id);
                return new ResourceNotFoundException("Bug not found with id: " + id);
            });

        bugRepository.delete(bug);
        log.info("Bug deleted successfully with id: {}", id);

        // Invalidate cache after delete
        cacheManager.invalidateBugCache(id);
        log.info("Cache invalidated for deleted bug - id: {}", id);
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

