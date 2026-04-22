package com.usermanagement.service;

import com.usermanagement.dto.UpdateUserRequest;
import com.usermanagement.dto.UserResponse;
import com.usermanagement.caching.UserCacheManager;
import com.usermanagement.entity.User;
import com.usermanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserCacheManager userCacheManager;

    public UserService(UserRepository userRepository, UserCacheManager userCacheManager) {
        this.userRepository = userRepository;
        this.userCacheManager = userCacheManager;
    }

    // not used redis on getAllUser becuase 
    // 1) can multiple key possible diffculty to delete
    // 2) frequently change (because of crud)
    public Page<UserResponse> getAllUsers(String correlationId, int page, int size) {
        log.info("correlationId: {} - Fetching users with page: {}, size: {}", correlationId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> response = userRepository.findAll(pageable).map(this::mapToUserResponse);
        log.info("correlationId: {} - Fetched {} users", correlationId, response.getNumberOfElements());
        return response;
    }

    public UserResponse getUserById(String correlationId, Long id) {
        log.info("correlationId: {} - Fetching user with id: {}", correlationId, id);
        UserResponse cachedUser = userCacheManager.getCachedUserById(correlationId, id);
        if (cachedUser != null) {
            log.info("correlationId: {} - User retrieved from cache - id: {}", correlationId, id);
            return cachedUser;
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("correlationId: {} - User not found with id: {}", correlationId, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        UserResponse userResponse = mapToUserResponse(user);
        userCacheManager.cacheUser(correlationId, userResponse);
        log.info("correlationId: {} - User cached after database fetch - id: {}", correlationId, id);
        return userResponse;
    }

    public UserResponse getUserByEmail(String correlationId, String email) {
        log.info("correlationId: {} - Fetching user with email: {}", correlationId, email);
        UserResponse cachedUser = userCacheManager.getCachedUserByEmail(correlationId, email);
        if (cachedUser != null) {
            log.info("correlationId: {} - User retrieved from cache - email: {}", correlationId, email);
            return cachedUser;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("correlationId: {} - User not found with email: {}", correlationId, email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        UserResponse userResponse = mapToUserResponse(user);
        userCacheManager.cacheUser(correlationId, userResponse);
        log.info("correlationId: {} - User cached after database fetch - email: {}", correlationId, email);
        return userResponse;
    }


    // cann't use email for "find" or "delete" api becuase email can be updated (so,mutable )
    public UserResponse updateUser(String correlationId, Long id, UpdateUserRequest request) {
        log.info("correlationId: {} - Updating user with id: {}", correlationId, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("correlationId: {} - User not found with id: {}", correlationId, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        String originalEmail = user.getEmail();
        user.setEmail(request.email());
        user = userRepository.save(user);
        log.info("correlationId: {} - User updated successfully with id: {}", correlationId, id);

        userCacheManager.evictCache(correlationId, originalEmail, id);
        UserResponse userResponse = mapToUserResponse(user);
        userCacheManager.cacheUser(correlationId, userResponse);
        return userResponse;
    }

    public void deleteUser(String correlationId, Long id) {
        log.info("correlationId: {} - Deleting user with id: {}", correlationId, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("correlationId: {} - User not found with id: {}", correlationId, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        userRepository.deleteById(id);
        log.info("correlationId: {} - User deleted successfully with id: {}", correlationId, id);
        userCacheManager.evictCache(correlationId, user.getEmail(), id);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
