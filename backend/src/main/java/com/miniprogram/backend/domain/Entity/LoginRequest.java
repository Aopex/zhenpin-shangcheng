package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 微信会话密钥
     */
    @NotNull(message = "会话密钥不能为空")
    private String sessionKey;
}
