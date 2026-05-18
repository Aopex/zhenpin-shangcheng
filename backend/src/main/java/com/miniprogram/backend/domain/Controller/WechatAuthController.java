package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.domain.Entity.LoginResponse;
import com.miniprogram.backend.domain.Entity.WxLoginRequest;
import com.miniprogram.backend.domain.Service.UserSessionService;
import com.miniprogram.backend.domain.Service.UserService;
import com.miniprogram.backend.service.WechatAuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 微信认证控制器
 * 处理微信小程序登录相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class WechatAuthController {
    
    @Autowired
    private WechatAuthService wechatAuthService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserSessionService userSessionService;
    
    /**
     * 微信一键登录（注册+登录合并）
     * 
     * 流程：
     * 1. 前端调用 wx.login() 获取 code
     * 2. 前端将 code 发送到后端
     * 3. 后端用 code 换取 openid 和 session_key
     * 4. 后端根据 openid 查询或创建用户
     * 5. 后端生成 JWT Token 并返回
     * 
     * @param request 包含 code 和可选的用户信息
     * @return 登录响应，包含 token 和用户信息
     */
    @PostMapping("/wx-login")
    public ResponseEntity<ApiResponse<LoginResponse>> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        log.info("WeChat login request received");
        
        try {
            // 1. 使用 code 换取 openid 和 session_key
            WechatAuthService.WxSessionResponse wxResponse = wechatAuthService.code2Session(request.getCode());
            
            // 2. 根据 openid 查询或创建用户
            // 如果用户不存在，自动创建；如果存在，更新用户信息
            var userDTO = userService.getOrCreateUserByOpenid(
                wxResponse.getOpenid(),
                request.getNickname(),
                request.getAvatarUrl(),
                request.getGender()
            );
            
            // 3. 生成 JWT Token 并创建登录响应
            LoginResponse loginResponse = userSessionService.createLoginResponse(
                userDTO.getId(),
                wxResponse.getSessionKey()
            );
            
            log.info("WeChat login successful - userId: {}, openid: {}", 
                    userDTO.getId(), wxResponse.getOpenid());
            
            return ResponseEntity.ok(ApiResponse.success(loginResponse));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid login request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Configuration error: {}", e.getMessage());
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Server configuration error"));
        } catch (Exception e) {
            log.error("WeChat login failed", e);
            return ResponseEntity.status(500).body(ApiResponse.error(500, "Login failed: " + e.getMessage()));
        }
    }
}
