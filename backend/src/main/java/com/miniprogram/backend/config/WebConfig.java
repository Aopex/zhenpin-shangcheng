package com.miniprogram.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 * 配置跨域访问（CORS）、拦截器、静态资源映射和其他 Web 相关设置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthInterceptor authInterceptor;
    
    /**
     * 配置跨域访问
     * 允许前端小程序访问后端接口
     * 注意：生产环境应通过环境变量 ALLOWED_ORIGINS 配置具体域名
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 从环境变量获取允许的域名，默认为所有（开发环境）
        String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
        
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            // 生产环境：使用配置的域名
            String[] origins = allowedOrigins.split(",");
            registry.addMapping("/api/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        } else {
            // 开发环境：允许所有来源（方便调试）
            registry.addMapping("/api/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }
    
    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/orders/**", "/api/cart/**", "/api/users/me")  // 需要认证的路径
                .excludePathPatterns("/api/users/register", "/api/users/*/login", "/api/products/**", "/api/sessions/**");  // 排除的路径
    }
    
    /**
     * 配置静态资源映射
     * 将 /pics/** 请求映射到 resources/static/pics/ 目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 商品图片资源映射
        // 访问 http://localhost:8080/pics/xxx.jpg 会映射到 classpath:/static/pics/xxx.jpg
        registry.addResourceHandler("/pics/**")
                .addResourceLocations("classpath:/static/pics/");
        
        // 也可以配置其他静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
