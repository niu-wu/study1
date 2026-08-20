package com.example.study11.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置,集中管理 Mapper 扫描路径
 * 放在独立配置类而非启动类,便于 Web 切片测试隔离持久层
 */
@Configuration
@MapperScan("com.example.study11.dao")
public class MyBatisConfig {
}
