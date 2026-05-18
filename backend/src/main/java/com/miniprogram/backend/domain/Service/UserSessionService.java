package com.miniprogram.backend.domain.Service;

import com.miniprogram.backend.common.BusinessException;
import com.miniprogram.backend.config.JwtUtil;
import com.miniprogram.backend.domain.Entity.LoginResponse;
import com.miniprogram.backend.domain.Entity.User;
import com.miniprogram.backend.service.RedisSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户会话服务类（简化版 - 仅用于登录流程）
 * 
 * 注意：此服务现在仅负责创建登录响应，会话管理已完全移至 Redis
 */
@Slf4j
@Service
public class UserSessionService {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RedisSessionService redisSessionService;
    
    /**
     * 创建登录响应（包含 JWT Token）
     * 
     * @param userId 用户ID
     * @param sessionKey 微信 session_key
     * @return 登录响应
     */
    @Transactional
    public LoginResponse createLoginResponse(Long userId, String sessionKey) {
        log.info("Creating login response for user id: {}", userId);
        
        if (userId == null) {
            throw new BusinessException(400, "User ID cannot be null");
        }
        if (!StringUtils.hasText(sessionKey)) {
            throw new BusinessException(400, "Session key cannot be empty");
        }
        
        // 获取用户信息
        User user = userService.getUserEntityById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found with id: " + userId);
        }
        
        // 从数据库读取用户角色（默认为USER）
        String role = user.getRole() != null ? user.getRole() : "USER";
        log.info("User login - userId: {}, role: {}", userId, role);
        
        // 生成 JWT Token（包含角色）
        String jwtToken = jwtUtil.generateToken(userId, user.getOpenid(), role);
        
        // 在 Redis 中存储会话
        redisSessionService.createSession(userId, sessionKey, user.getOpenid());
        
        // 构建登录响应
        LoginResponse response = new LoginResponse();
        response.setUserId(userId);
        response.setOpenid(user.getOpenid());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setToken(jwtToken);
        response.setSessionKey(sessionKey);
        
        log.info("Login response created successfully for user id: {}", userId);
        return response;
    }
}
