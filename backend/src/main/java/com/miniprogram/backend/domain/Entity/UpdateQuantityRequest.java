package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新购物车数量请求DTO
 */
@Data
public class UpdateQuantityRequest {
    
    /**
     * 新的数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}
