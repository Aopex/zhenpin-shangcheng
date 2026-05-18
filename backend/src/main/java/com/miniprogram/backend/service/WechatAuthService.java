package com.miniprogram.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信认证服务
 * 负责与微信服务器交互，完成 code 换取 openid 和 session_key
 */
@Slf4j
@Service
public class WechatAuthService {
    
    @Value("${wechat.appid:}")
    private String appid;
    
    @Value("${wechat.secret:}")
    private String secret;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 微信 code2Session API 地址
     */
    private static final String CODE2SESSION_URL = 
        "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    
    /**
     * 使用微信登录 code 换取 openid 和 session_key
     * 
     * @param code 微信登录凭证
     * @return WxSessionResponse 包含 openid 和 session_key
     */
    public WxSessionResponse code2Session(String code) {
        log.info("Exchanging code for openid and session_key");
        
        // 参数校验
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        
        // 检查配置
        if (appid == null || appid.isEmpty() || secret == null || secret.isEmpty()) {
            log.error("WeChat appid or secret not configured");
            throw new IllegalStateException("WeChat configuration is missing. Please set wechat.appid and wechat.secret");
        }
        
        try {
            // 构建请求 URL
            String url = String.format(CODE2SESSION_URL, appid, secret, code);
            
            // 调用微信 API
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                // 检查是否有错误
                if (jsonNode.has("errcode")) {
                    int errcode = jsonNode.get("errcode").asInt();
                    String errmsg = jsonNode.get("errmsg").asText();
                    log.error("WeChat API error - errcode: {}, errmsg: {}", errcode, errmsg);
                    throw new RuntimeException("WeChat API error: " + errmsg);
                }
                
                // 提取 openid 和 session_key
                String openid = jsonNode.get("openid").asText();
                String sessionKey = jsonNode.get("session_key").asText();
                
                log.info("Successfully exchanged code for openid: {}", openid);
                
                return new WxSessionResponse(openid, sessionKey);
            } else {
                throw new RuntimeException("Failed to call WeChat API");
            }
        } catch (Exception e) {
            log.error("Error exchanging code for session", e);
            throw new RuntimeException("Failed to exchange code: " + e.getMessage(), e);
        }
    }
    
    /**
     * 微信会话响应
     */
    public static class WxSessionResponse {
        private String openid;
        private String sessionKey;
        
        public WxSessionResponse() {}
        
        public WxSessionResponse(String openid, String sessionKey) {
            this.openid = openid;
            this.sessionKey = sessionKey;
        }
        
        public String getOpenid() {
            return openid;
        }
        
        public void setOpenid(String openid) {
            this.openid = openid;
        }
        
        public String getSessionKey() {
            return sessionKey;
        }
        
        public void setSessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
        }
    }
}
