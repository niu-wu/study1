package com.example.study11.service;

import com.example.study11.entity.dto.ChangePasswordDTO;

/**
 * 修改密码业务接口。
 */
public interface PasswordChangeService {

    /**
     * 执行一次完整的修改密码流程。
     *
     * @param userId 当前登录用户 ID
     * @param request 修改密码参数
     * @param clientIp 客户端 IP
     */
    void changePassword(Integer userId, ChangePasswordDTO request, String clientIp);
}
