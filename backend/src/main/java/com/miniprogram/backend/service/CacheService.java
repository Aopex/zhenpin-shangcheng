package com.miniprogram.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 通用缓存服务
 * 提供基于Redis的缓存操作
 * 支持Redis故障降级：如果Redis不可用，不影响正常业务
 */
@Slf4j
@Service
public class CacheService {
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 获取缓存
     * @param key 缓存键
     * @param clazz 目标类型
     * @return 缓存的对象，如果不存在或解析失败则返回null
     */
    public <T> T get(String key, Class<T> clazz) {
        try {
            if (redisTemplate == null) {
                log.debug("Redis not available, cache miss for key: {}", key);
                return null;
            }
            
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            
            // 如果已经是目标类型，直接返回
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            
            // 否则尝试JSON反序列化
            String json = objectMapper.writeValueAsString(value);
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to get cache for key: {} (Redis error). Returning null.", key, e);
            return null;
        }
    }
    
    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (redisTemplate == null) {
                log.debug("Redis not available, cache set skipped for key: {}", key);
                return;
            }
            
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Cache set for key: {}, timeout: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Failed to set cache for key: {} (Redis error). Operation skipped.", key, e);
        }
    }
    
    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            if (redisTemplate == null) {
                log.debug("Redis not available, cache delete skipped for key: {}", key);
                return;
            }
            
            redisTemplate.delete(key);
            log.debug("Cache deleted for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete cache for key: {} (Redis error). Operation skipped.", key, e);
        }
    }
    
    /**
     * 检查缓存是否存在
     * @param key 缓存键
     * @return true-存在，false-不存在
     */
    public Boolean hasKey(String key) {
        try {
            if (redisTemplate == null) {
                log.debug("Redis not available, cache key check skipped for key: {}", key);
                return false;
            }
            
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Failed to check cache key: {} (Redis error). Returning false.", key, e);
            return false;
        }
    }
    
    /**
     * 尝试获取分布式锁（防止缓存击穿）
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @return true-获取成功，false-获取失败
     */
    public boolean tryLock(String lockKey, long expireTime) {
        try {
            if (redisTemplate == null) {
                log.debug("Redis not available, lock skipped for key: {}", lockKey);
                return true; // Redis不可用时，直接返回成功，不影响业务
            }
            
            // setIfAbsent相当于SETNX，只有key不存在时才设置
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", expireTime, TimeUnit.SECONDS);
            return success != null && success;
        } catch (Exception e) {
            log.error("Failed to acquire lock for key: {} (Redis error). Returning false.", lockKey, e);
            return false;
        }
    }
    
    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        try {
            if (redisTemplate == null) {
                log.debug("Redis not available, unlock skipped for key: {}", lockKey);
                return;
            }
            
            redisTemplate.delete(lockKey);
            log.debug("Lock released for key: {}", lockKey);
        } catch (Exception e) {
            log.error("Failed to release lock for key: {} (Redis error). Operation skipped.", lockKey, e);
        }
    }
}
