package com.miniprogram.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 * 使用Redis存储被注销的Token，实现主动登出功能
 * 支持Redis故障降级：如果Redis不可用，不影响正常业务
 */
@Slf4j
@Service
public class TokenBlacklistService {
    
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    
    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 将Token加入黑名单（登出时调用）
     * @param token JWT Token
     * @param expirationInMillis Token剩余有效期（毫秒）
     */
    public void addToBlacklist(String token, long expirationInMillis) {
        if (expirationInMillis <= 0) {
            log.warn("Token already expired, no need to add to blacklist");
            return;
        }
        
        // Redis降级处理：如果Redis不可用，记录警告但不影响业务
        try {
            if (stringRedisTemplate == null) {
                log.warn("Redis not available, token blacklist disabled");
                return;
            }
            
            String key = BLACKLIST_PREFIX + token;
            stringRedisTemplate.opsForValue().set(key, "invalidated", expirationInMillis, TimeUnit.MILLISECONDS);
            log.info("Token added to blacklist, will expire in {} ms", expirationInMillis);
        } catch (Exception e) {
            log.error("Failed to add token to blacklist (Redis error): {}. Token will remain valid until expiration.", 
                     e.getMessage());
            // 不抛出异常，允许用户继续操作，Token会在自然过期后失效
        }
    }
    
    /**
     * 检查Token是否在黑名单中
     * @param token JWT Token
     * @return true-在黑名单中（已失效），false-不在黑名单中（有效）
     *         如果Redis不可用，返回false（允许访问）
     */
    public boolean isBlacklisted(String token) {
        try {
            if (stringRedisTemplate == null) {
                log.debug("Redis not available, skipping blacklist check");
                return false; // Redis不可用时，不做黑名单检查
            }
            
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = stringRedisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check token blacklist (Redis error): {}. Allowing access.", e.getMessage());
            return false; // Redis错误时，允许访问
        }
    }
    
    /**
     * 从黑名单中移除Token（一般不需要）
     * @param token JWT Token
     */
    public void removeFromBlacklist(String token) {
        try {
            if (stringRedisTemplate == null) {
                log.warn("Redis not available, cannot remove from blacklist");
                return;
            }
            
            String key = BLACKLIST_PREFIX + token;
            stringRedisTemplate.delete(key);
            log.info("Token removed from blacklist");
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist: {}", e.getMessage());
        }
    }
    
    /**
     * 获取黑名单中的Token数量（用于监控）
     * @return 黑名单Token数量
     */
    public long getBlacklistCount() {
        try {
            if (stringRedisTemplate == null) {
                return 0;
            }
            return stringRedisTemplate.keys(BLACKLIST_PREFIX + "*").size();
        } catch (Exception e) {
            log.error("Failed to get blacklist count: {}", e.getMessage());
            return 0;
        }
    }
}
