package com.miniprogram.backend.config;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.common.RequireAdmin;
import com.miniprogram.backend.common.RequirePermission;
import com.miniprogram.backend.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 用于验证请求中的JWT Token，并检查是否在黑名单中
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求头中的Token
        String token = request.getHeader("Authorization");
        
        // 如果Token为空，尝试从参数中获取
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        
        // 检查Token是否存在
        if (token == null || token.isEmpty()) {
            log.warn("No authorization token provided for request: {}", request.getRequestURI());
            throw new BusinessException(401, "Authorization token is required");
        }
        
        // 去除Bearer前缀（如果有）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 验证JWT签名和有效期
        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid or expired token for request: {}", request.getRequestURI());
            throw new BusinessException(401, "Invalid or expired token");
        }
        
        // 检查Token是否在黑名单中（Redis）
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn("Token is blacklisted for request: {}", request.getRequestURI());
            throw new BusinessException(401, "Token has been invalidated");
        }
        
        // 将用户ID存入请求属性，供后续使用
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId != null) {
            request.setAttribute("userId", userId);
            log.debug("User authenticated successfully - userId: {}", userId);
        }
        
        // 检查权限控制注解
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            
            // 检查是否需要管理员权限（@RequireAdmin）
            RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);
            if (requireAdmin == null) {
                requireAdmin = handlerMethod.getBeanType().getAnnotation(RequireAdmin.class);
            }
            
            if (requireAdmin != null) {
                if (!jwtUtil.isAdmin(token)) {
                    log.warn("User {} attempted to access admin endpoint: {}", userId, request.getRequestURI());
                    throw new BusinessException(403, "Admin permission required");
                }
                log.debug("Admin access granted - userId: {}", userId);
                return true;
            }
            
            // 检查细粒度权限控制（@RequirePermission）
            RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
            if (requirePermission == null) {
                requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
            }
            
            if (requirePermission != null) {
                // 如果需要管理员权限
                if (requirePermission.adminOnly() && !jwtUtil.isAdmin(token)) {
                    log.warn("User {} attempted to access admin-only endpoint: {}", userId, request.getRequestURI());
                    throw new BusinessException(403, "Admin permission required");
                }
                
                // TODO: 这里可以扩展为检查用户的具体权限
                // 目前简化实现：只要登录即可访问有权限注解的接口
                // 未来可以从数据库或Redis中查询用户的权限列表进行验证
                log.debug("Permission check passed - userId: {}, permission: {}", userId, requirePermission.value());
            }
        }
        
        return true;
    }
}
