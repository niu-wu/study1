package com.example.study11.service;

import com.example.study11.entity.po.UserPo;

import java.time.LocalDateTime;

/**
 * 密码修改后的通知接口。
 */
public interface NotificationService {

    /**
     * 发送密码修改成功通知。
     *
     * @param user 修改前的用户快照，避免把新密码等敏感字段传入通知实现
     * @param clientIp 发起操作的 IP
     * @param changedAt 操作时间
     */
    void sendPasswordChanged(UserPo user, String clientIp, LocalDateTime changedAt);
}
