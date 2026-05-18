package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单操作请求DTO（用于取消、支付、确认收货等操作）
 */
@Data
public class OrderActionRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
