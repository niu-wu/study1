package com.example.study11.service.impl;

import com.example.study11.config.PasswordConfig;
import com.example.study11.entity.po.UserPo;
import com.example.study11.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 密码修改邮件通知实现。
 *
 * <p>邮件配置只从环境变量/application.yml 读取，不在代码中写入真实 SMTP 凭据。
 * 未配置邮箱或邮件组件时记录告警并返回，避免阻塞已经成功的密码修改。</p>
 */
@Slf4j
@Service
public class EmailNotificationService implements NotificationService {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 邮件组件是可选依赖，未配置 SMTP 时仍允许核心服务启动。 */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private PasswordConfig passwordConfig;

    @Override
    public void sendPasswordChanged(UserPo user, String clientIp, LocalDateTime changedAt) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("用户未配置邮箱，跳过密码修改通知: userId={}", user == null ? null : user.getId());
            return;
        }
        if (mailSender == null) {
            log.warn("未配置邮件发送组件，跳过密码修改通知: userId={}", user.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        String from = passwordConfig == null ? null : passwordConfig.getMailFrom();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(user.getEmail());
        message.setSubject("密码修改成功通知");
        message.setText("您好，您的账号 " + safe(user.getUsername()) + " 已于 "
                + TIME_FORMATTER.format(changedAt) + " 完成密码修改。\n"
                + "操作 IP：" + safe(clientIp) + "\n"
                + "如非本人操作，请立即联系管理员。");
        mailSender.send(message);
    }

    /** 邮件中只展示脱敏后的可读字段，避免空值形成误导。 */
    private String safe(String value) {
        return value == null || value.isBlank() ? "未知" : value;
    }
}
