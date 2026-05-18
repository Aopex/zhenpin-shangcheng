package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求DTO
 */
@Data
public class WxLoginRequest {
    
    /**
     * 微信登录凭证 code
     */
    @NotBlank(message = "Code cannot be empty")
    private String code;
    
    /**
     * 用户昵称（可选，首次注册时使用）
     */
    private String nickname;
    
    /**
     * 头像URL（可选，首次注册时使用）
     */
    private String avatarUrl;
    
    /**
     * 性别（可选，首次注册时使用）
     * 0-未知, 1-男, 2-女
     */
    private Integer gender;
}
