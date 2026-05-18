package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加到购物车请求DTO
 */
@Data
public class AddToCartRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空")
    private Long productId;
    
    /**
     * SKU ID（可选）
     */
    private Long skuId;
    
    /**
     * 购买数量
     */
    @Min(value = 1, message = "购买数量必须大于0")
    private Integer quantity = 1;
}
