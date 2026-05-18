package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.config.JwtUtil;
import com.miniprogram.backend.domain.Entity.LoginRequest;
import com.miniprogram.backend.domain.Entity.LogoutRequest;
import com.miniprogram.backend.domain.Entity.LoginResponse;
import com.miniprogram.backend.domain.Service.UserSessionService;
import com.miniprogram.backend.service.RedisSessionService;
import com.miniprogram.backend.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户会话控制器（JWT + Redis）
 */
@Slf4j
@RestController
@RequestMapping("/api/sessions")
public class UserSessionController {
    
    @Autowired
    private UserSessionService userSessionService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Autowired
    private RedisSessionService redisSessionService;
    
    /**
     * 登录并返回 JWT Token
     * 
     * @param request 登录请求
     * @return 登录响应，包含 JWT Token 和用户信息
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userSessionService.createLoginResponse(request.getUserId(), request.getSessionKey());
        
        log.info("User logged in successfully - userId: {}", request.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 登出（将 Token 加入黑名单，删除 Redis 会话）
     * 
     * @param authorization Authorization header
     * @param request 包含 userId
     * @return 成功响应
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody LogoutRequest request) {
        // 获取 Token
        String token = authorization;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 获取用户 ID
        Long userId = request.getUserId();
        
        // 1. 将 Token 加入黑名单（Redis）
        Claims claims = jwtUtil.getClaimsFromToken(token);
        if (claims != null) {
            long expiration = claims.getExpiration().getTime() - System.currentTimeMillis();
            tokenBlacklistService.addToBlacklist(token, expiration);
        }
        
        // 2. 删除 Redis 中的会话
        if (userId != null) {
            redisSessionService.deleteSession(userId);
        }
        
        log.info("User logged out successfully - userId: {}", userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return true-有效，false-无效
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        // 去除 Bearer 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 1. 验证 JWT 签名和有效期
        boolean isValid = jwtUtil.validateToken(token);
        
        // 2. 检查是否在黑名单中
        if (isValid && tokenBlacklistService.isBlacklisted(token)) {
            isValid = false;
        }
        
        return ResponseEntity.ok(ApiResponse.success(isValid));
    }
}
