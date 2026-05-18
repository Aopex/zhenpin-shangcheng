package com.miniprogram.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT Token
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // 默认24小时
    private Long expiration;
    
    /**
     * 启动时验证JWT密钥配置
     */
    @PostConstruct
    public void validateJwtSecret() {
        if (secret == null || secret.isEmpty()) {
            log.error("========================================");
            log.error("JWT_SECRET 环境变量未设置！");
            log.error("请在生产环境中设置强密钥（至少32字符）");
            log.error("示例: export JWT_SECRET=$(openssl rand -base64 64)");
            log.error("========================================");
            throw new IllegalStateException("JWT_SECRET must be set in production environment");
        }
        
        if (secret.length() < 32) {
            log.warn("JWT密钥长度不足32字符，建议使用更强的密钥");
        }
        
        log.info("JWT密钥验证通过，密钥长度: {} 字符", secret.length());
    }
    
    /**
     * 生成密钥
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param openid 微信openid
     * @param role 用户角色（USER/ADMIN）
     * @return JWT Token
     */
    public String generateToken(Long userId, String openid, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("openid", openid);
        claims.put("role", role != null ? role : "USER");  // 默认角色为USER
        
        return createToken(claims, userId.toString());
    }
    
    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSignKey())
                .compact();
    }
    
    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        } else if (userId instanceof String) {
            return Long.parseLong((String) userId);
        }
        
        return null;
    }
    
    /**
     * 从Token中获取openid
     */
    public String getOpenidFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        
        return claims.get("openid", String.class);
    }
    
    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        
        return claims.get("role", String.class);
    }
    
    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin(String token) {
        String role = getRoleFromToken(token);
        return "ADMIN".equals(role);
    }
    
    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return false;
            }
            
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());
        } catch (Exception e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        
        Long userId = getUserIdFromToken(token);
        String openid = getOpenidFromToken(token);
        String role = getRoleFromToken(token);
        
        if (userId == null || openid == null) {
            return null;
        }
        
        return generateToken(userId, openid, role);
    }
}
