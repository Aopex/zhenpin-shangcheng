package com.miniprogram.backend.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 用于从当前请求中获取认证用户信息
 */
public class UserContext {
    
    /**
     * 从当前请求中获取用户ID
     * @return 用户ID
     * @throws BusinessException 如果用户未认证
     */
    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            throw new BusinessException(401, "No active request context");
        }
        
        HttpServletRequest request = attributes.getRequest();
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            throw new BusinessException(401, "User not authenticated");
        }
        
        return userId;
    }
    
    /**
     * 安全地获取用户ID（不抛异常）
     * @return 用户ID，如果未认证则返回null
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
