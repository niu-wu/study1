package com.example.study11.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 修改密码功能的可配置参数。
 *
 * <p>配置集中放在这里，业务代码不直接散落阈值，便于按环境调整并在测试中覆盖边界值。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "password.change")
public class PasswordConfig {

    /** 每个用户每天允许提交的错误旧密码次数。第 7 次提交会被拒绝。 */
    private int maxWrongAttempts = 6;

    /** 从第几次错误开始要求图形验证码；3 表示第 4 次开始校验。 */
    private int captchaThreshold = 3;

    /** 每个用户每天允许成功修改密码的次数。 */
    private int maxDailyChanges = 3;

    /** 图形验证码在 Redis 中的有效时间，单位为秒。 */
    private long captchaExpireSeconds = 300;

    /** 邮件发件人地址；未配置时由 SMTP 客户端的默认地址决定。 */
    private String mailFrom;
}
