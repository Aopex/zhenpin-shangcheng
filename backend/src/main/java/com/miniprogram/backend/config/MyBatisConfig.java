package com.miniprogram.backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 * 扫描 Mapper 接口所在的包
 */
@Configuration
@MapperScan("com.miniprogram.backend.domain.Mapper")
public class MyBatisConfig {
}
