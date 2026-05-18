package com.miniprogram.backend.domain.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 用户 DTO 类
 * 用于 API 数据传输和参数校验
 */
public class UserDTO {
    
    private Long id;
    
    @NotBlank(message = "OpenID 不能为空")
    private String openid;
    
    private String nickname;
    
    private String avatarUrl;
    
    private Integer gender = 0; // 0-未知, 1-男, 2-女
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    private Integer status = 1; // 0-禁用, 1-正常
    
    private LocalDateTime lastLoginTime;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    // 构造函数
    public UserDTO() {}
    
    public UserDTO(User user) {
        this.id = user.getId();
        this.openid = user.getOpenid();
        this.nickname = user.getNickname();
        this.avatarUrl = user.getAvatarUrl();
        this.gender = user.getGender();
        this.phone = user.getPhone();
        this.status = user.getStatus();
        this.lastLoginTime = user.getLastLoginTime();
        this.createTime = user.getCreateTime();
        this.updateTime = user.getUpdateTime();
    }
    
    /**
     * 转换为 Entity
     */
    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setOpenid(this.openid);
        user.setNickname(this.nickname);
        user.setAvatarUrl(this.avatarUrl);
        user.setGender(this.gender);
        user.setPhone(this.phone);
        user.setStatus(this.status);
        user.setLastLoginTime(this.lastLoginTime);
        return user;
    }
    
    // Getter 和 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOpenid() {
        return openid;
    }
    
    public void setOpenid(String openid) {
        this.openid = openid;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Integer getGender() {
        return gender;
    }
    
    public void setGender(Integer gender) {
        this.gender = gender;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }
    
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
