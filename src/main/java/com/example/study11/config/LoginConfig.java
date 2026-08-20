package com.example.study11.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 登录相关配置,映射 sso.* 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "sso")
public class LoginConfig {

    // AES 对称加密密钥，长度必须为 16、24 或 32 个字节
    private String secret;

    // 登录开关
    private Boolean loginFlag;

    // Token 有效期，单位：秒
    private Long tokenExpireSeconds;
}
