package com.miniprogram.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Redis会话服务
 * 使用Redis存储用户会话信息，替代数据库session表
 */
@Slf4j
@Service
public class RedisSessionService {
    
    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_EXPIRE_DAYS = 7; // 会话有效期7天
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    // Jackson ObjectMapper用于JSON序列化（比FastJSON更安全）
    private final ObjectMapper objectMapper;
    
    public RedisSessionService() {
        this.objectMapper = new ObjectMapper();
        // 注册JavaTimeModule以支持LocalDateTime等Java 8时间类型
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * 会话数据
     */
    @Data
    public static class SessionData {
        private Long userId;
        private String sessionKey;
        private String openid;
        private LocalDateTime loginTime;
        private LocalDateTime lastActiveTime;
        
        public SessionData() {}
        
        public SessionData(Long userId, String sessionKey, String openid) {
            this.userId = userId;
            this.sessionKey = sessionKey;
            this.openid = openid;
            this.loginTime = LocalDateTime.now();
            this.lastActiveTime = LocalDateTime.now();
        }
        
        public void updateLastActiveTime() {
            this.lastActiveTime = LocalDateTime.now();
        }
    }
    
    /**
     * 创建会话
     * @param userId 用户ID
     * @param sessionKey 微信session_key
     * @param openid 微信openid
     * @return 会话数据
     */
    public SessionData createSession(Long userId, String sessionKey, String openid) {
        String key = SESSION_PREFIX + userId;
        
        SessionData sessionData = new SessionData(userId, sessionKey, openid);
        
        try {
            String json = objectMapper.writeValueAsString(sessionData);
            // 存入Redis，设置过期时间
            stringRedisTemplate.opsForValue().set(key, json, SESSION_EXPIRE_DAYS, TimeUnit.DAYS);
            log.info("Session created in Redis for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize session data", e);
            throw new RuntimeException("Failed to create session", e);
        }
        
        return sessionData;
    }
    
    /**
     * 获取会话
     * @param userId 用户ID
     * @return 会话数据，不存在则返回null
     */
    public SessionData getSession(Long userId) {
        if (userId == null) {
            return null;
        }
        
        String key = SESSION_PREFIX + userId;
        String json = stringRedisTemplate.opsForValue().get(key);
        
        if (json == null || json.isEmpty()) {
            log.debug("Session not found in Redis for user: {}", userId);
            return null;
        }
        
        try {
            SessionData sessionData = objectMapper.readValue(json, SessionData.class);
            log.debug("Session retrieved from Redis for user: {}", userId);
            return sessionData;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse session data from Redis", e);
            return null;
        }
    }
    
    /**
     * 更新会话活跃时间
     * @param userId 用户ID
     */
    public void updateSessionActivity(Long userId) {
        SessionData sessionData = getSession(userId);
        if (sessionData != null) {
            sessionData.updateLastActiveTime();
            String key = SESSION_PREFIX + userId;
            
            try {
                String json = objectMapper.writeValueAsString(sessionData);
                // 重新存入，刷新过期时间
                stringRedisTemplate.opsForValue().set(key, json, SESSION_EXPIRE_DAYS, TimeUnit.DAYS);
                log.debug("Session activity updated for user: {}", userId);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize session data", e);
            }
        }
    }
    
    /**
     * 删除会话（登出时调用）
     * @param userId 用户ID
     */
    public void deleteSession(Long userId) {
        if (userId == null) {
            return;
        }
        
        String key = SESSION_PREFIX + userId;
        stringRedisTemplate.delete(key);
        log.info("Session deleted from Redis for user: {}", userId);
    }
    
    /**
     * 检查会话是否存在
     * @param userId 用户ID
     * @return true-存在，false-不存在
     */
    public boolean hasSession(Long userId) {
        if (userId == null) {
            return false;
        }
        
        String key = SESSION_PREFIX + userId;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
