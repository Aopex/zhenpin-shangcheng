package com.miniprogram.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.miniprogram.backend.domain.Entity.UserDTO;
import com.miniprogram.backend.domain.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户信息缓存服务
 * 使用Redis缓存用户信息，减少数据库查询
 */
@Slf4j
@Service
public class UserCacheService {
    
    private static final String USER_INFO_PREFIX = "user:info:";
    private static final long CACHE_EXPIRE_MINUTES = 30; // 缓存30分钟
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private UserService userService;
    
    // Jackson ObjectMapper用于JSON序列化（比FastJSON更安全）
    private final ObjectMapper objectMapper;
    
    public UserCacheService() {
        this.objectMapper = new ObjectMapper();
        // 注册JavaTimeModule以支持LocalDateTime等Java 8时间类型
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * 获取用户信息（先查缓存，再查数据库）
     * @param userId 用户ID
     * @return 用户信息DTO
     */
    public UserDTO getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        
        String key = USER_INFO_PREFIX + userId;
        
        try {
            // 1. 尝试从Redis获取
            String cached = stringRedisTemplate.opsForValue().get(key);
            if (cached != null && !cached.isEmpty()) {
                log.debug("Cache hit for user: {}", userId);
                return objectMapper.readValue(cached, UserDTO.class);
            }
            
            // 2. 从数据库获取
            log.debug("Cache miss for user: {}, fetching from database", userId);
            UserDTO user = userService.getUserById(userId);
            
            if (user != null) {
                // 3. 存入缓存
                String json = objectMapper.writeValueAsString(user);
                stringRedisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                log.debug("User info cached for user: {}", userId);
            }
            
            return user;
        } catch (JsonProcessingException e) {
            log.error("Failed to process JSON for user cache", e);
            // 缓存失败时直接从数据库获取
            return userService.getUserById(userId);
        } catch (Exception e) {
            log.error("Failed to get user info from cache, fallback to database", e);
            // 缓存失败时直接从数据库获取
            return userService.getUserById(userId);
        }
    }
    
    /**
     * 清除用户缓存（用户信息更新时调用）
     * @param userId 用户ID
     */
    public void clearUserCache(Long userId) {
        if (userId == null) {
            return;
        }
        
        String key = USER_INFO_PREFIX + userId;
        stringRedisTemplate.delete(key);
        log.info("User cache cleared for user: {}", userId);
    }
    
    /**
     * 批量清除用户缓存
     * @param userIds 用户ID列表
     */
    public void clearUserCacheBatch(java.util.List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        
        String[] keys = userIds.stream()
                .map(id -> USER_INFO_PREFIX + id)
                .toArray(String[]::new);
        
        stringRedisTemplate.delete(java.util.Arrays.asList(keys));
        log.info("Batch user cache cleared for {} users", userIds.size());
    }
    
    /**
     * 手动刷新用户缓存
     * @param userId 用户ID
     */
    public void refreshUserCache(Long userId) {
        clearUserCache(userId);
        getUserInfo(userId);
        log.info("User cache refreshed for user: {}", userId);
    }
}
