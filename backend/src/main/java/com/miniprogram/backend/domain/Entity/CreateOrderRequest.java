package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求DTO
 */
@Data
public class CreateOrderRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 收货地址ID
     */
    @NotNull(message = "收货地址ID不能为空")
    private Long addressId;
    
    /**
     * 购物车项ID列表
     * 传 [0] 表示选择所有选中项
     */
    @NotEmpty(message = "购物车项ID列表不能为空")
    private List<Long> cartItemIds;
    
    /**
     * 订单备注（可选）
     */
    private String remark;
}
