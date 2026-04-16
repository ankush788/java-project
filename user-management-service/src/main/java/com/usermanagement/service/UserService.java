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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserCacheManager userCacheManager;

    public UserService(UserRepository userRepository, UserCacheManager userCacheManager) {
        this.userRepository = userRepository;
        this.userCacheManager = userCacheManager;
    }

    // not used redis on getAllUser becuase 
    // 1) can multiple key possible diffculty to delete
    // 2) frequently change (because of crud)
    public Page<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).map(this::mapToUserResponse);
    }

    public UserResponse getUserById(Long id) {
        UserResponse cachedUser = userCacheManager.getCachedUserById(id);
        if (cachedUser != null) {
            return cachedUser;
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserResponse userResponse = mapToUserResponse(user);
        userCacheManager.cacheUser(userResponse);
        return userResponse;
    }

    public UserResponse getUserByEmail(String email) {
        UserResponse cachedUser = userCacheManager.getCachedUserByEmail(email);
        if (cachedUser != null) {
            return cachedUser;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserResponse userResponse = mapToUserResponse(user);
        userCacheManager.cacheUser(userResponse);
        return userResponse;
    }


    // cann't use email for "find" or "delete" api becuase email can be updated (so,mutable )
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String originalEmail = user.getEmail();
        user.setEmail(request.email());
        user = userRepository.save(user);

        userCacheManager.evictCache(originalEmail, id);
        UserResponse userResponse = mapToUserResponse(user);
        userCacheManager.cacheUser(userResponse);
        return userResponse;
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.deleteById(id);
        userCacheManager.evictCache(user.getEmail(), id);
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