package com.miniprogram.backend.common;

import com.miniprogram.backend.domain.Entity.User;
import com.miniprogram.backend.domain.DAO.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员账户初始化器
 * 应用启动时自动创建管理员账户（如果不存在）
 */
@Slf4j
@Component
public class AdminInitializer implements CommandLineRunner {
    
    @Value("${admin.init.enabled:true}")
    private boolean adminInitEnabled;
    
    @Value("${admin.init.openid:admin_openid_001}")
    private String adminOpenid;
    
    @Value("${admin.init.nickname:系统管理员}")
    private String adminNickname;
    
    @Value("${admin.init.avatar-url:}")
    private String adminAvatarUrl;
    
    @Value("${admin.init.phone:}")
    private String adminPhone;
    
    private final UserRepository userRepository;
    
    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    @Transactional
    public void run(String... args) {
        if (!adminInitEnabled) {
            log.info("Admin initialization is disabled");
            return;
        }
        
        log.info("Checking admin account initialization...");
        
        // 检查管理员账户是否已存在
        var existingAdmin = userRepository.findByOpenid(adminOpenid);
        
        if (existingAdmin.isPresent()) {
            User admin = existingAdmin.get();
            // 确保角色为ADMIN
            if (!"ADMIN".equals(admin.getRole())) {
                admin.setRole("ADMIN");
                userRepository.save(admin);
                log.info("Updated existing user to ADMIN role - openid: {}, userId: {}", adminOpenid, admin.getId());
            } else {
                log.info("Admin account already exists - userId: {}", admin.getId());
            }
            return;
        }
        
        // 创建新的管理员账户
        User admin = new User();
        admin.setOpenid(adminOpenid);
        admin.setNickname(adminNickname);
        admin.setAvatarUrl(adminAvatarUrl);
        admin.setPhone(adminPhone);
        admin.setGender(0); // 未知
        admin.setStatus(1); // 正常状态
        admin.setRole("ADMIN"); // 设置为管理员角色
        
        User savedAdmin = userRepository.save(admin);
        
        log.info("✅ Admin account created successfully!");
        log.info("   - User ID: {}", savedAdmin.getId());
        log.info("   - OpenID: {}", adminOpenid);
        log.info("   - Nickname: {}", adminNickname);
        log.info("   - Role: ADMIN");
        log.info("");
        log.info("📝 使用说明：");
        log.info("   1. 使用此openid登录即可获得ADMIN权限");
        log.info("   2. 生产环境请修改application.yaml中的admin.init.openid配置");
        log.info("   3. 可通过设置admin.init.enabled=false禁用自动初始化");
    }
}
