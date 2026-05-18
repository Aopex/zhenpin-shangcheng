package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新选中状态请求DTO
 */
@Data
public class UpdateSelectedRequest {
    
    /**
     * 是否选中
     */
    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}
