package com.miniprogram.backend.domain.Entity;

/**
 * 登录响应DTO
 * 包含用户信息和JWT Token
 */
public class LoginResponse {
    private Long userId;
    private String openid;
    private String nickname;
    private String avatarUrl;
    private String token;
    private String sessionKey;
    
    public LoginResponse() {}
    
    public LoginResponse(Long userId, String openid, String nickname, String avatarUrl, String token, String sessionKey) {
        this.userId = userId;
        this.openid = openid;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.token = token;
        this.sessionKey = sessionKey;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getSessionKey() {
        return sessionKey;
    }
    
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
