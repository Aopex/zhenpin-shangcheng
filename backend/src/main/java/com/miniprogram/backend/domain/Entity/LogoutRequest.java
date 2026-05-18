package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 登出请求DTO
 */
@Data
public class LogoutRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
