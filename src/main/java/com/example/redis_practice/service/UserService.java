package com.example.redis_practice.service;

import com.example.redis_practice.entity.User;
import com.example.redis_practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @CachePut(cacheNames = "user", key = "#result.id")
    public User createUser(String username, Integer age) {
        User user = new User(username, age);
        return userRepository.save(user);
    }

    @CachePut(cacheNames = "user", key = "#id")
    public User updateUser(Long id, String username, Integer age) {
        User user = userRepository.findById(id)
                        .orElseThrow();

        user.setUsername(username);
        user.setAge(age);

        return user;
    }

    @CacheEvict(cacheNames = "user", key = "#id")
    public Long deleteUser(Long id) {
        userRepository.deleteById(id);
        return id;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Cacheable(cacheNames = "user", key = "#id")
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
