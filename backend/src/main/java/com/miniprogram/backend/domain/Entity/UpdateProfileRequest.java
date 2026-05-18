package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 当前登录用户资料更新请求
 */
@Data
public class UpdateProfileRequest {
    
    private String nickname;
    
    private String avatarUrl;
    
    private Integer gender;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
