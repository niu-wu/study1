package com.example.study11.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置。
 *
 * <p>BCrypt 只保存不可逆摘要，避免注册和修改密码时继续写入明文。</p>
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 创建全局 BCrypt 编码器。
     *
     * @return BCrypt 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
