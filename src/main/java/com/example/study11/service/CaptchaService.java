package com.example.study11.service;

import com.example.study11.entity.vo.CaptchaVO;

/**
 * 图形验证码业务接口。
 */
public interface CaptchaService {

    /**
     * 为指定用户生成一次性图形验证码。
     *
     * @param userId 当前登录用户 ID
     * @return 验证码编号和图片
     */
    CaptchaVO generate(Integer userId);

    /**
     * 校验验证码，并在校验成功后消费验证码。
     *
     * @param userId 当前登录用户 ID
     * @param captchaId 验证码编号
     * @param captchaCode 用户输入的答案
     * @return 验证通过返回 {@code true}
     */
    boolean validate(Integer userId, String captchaId, String captchaCode);
}
