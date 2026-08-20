package com.example.study11.service;

import com.example.study11.entity.dto.LoginDTO;
import com.example.study11.entity.dto.RegisterDTO;

/**
 * 登录注册业务层。
 */
public interface SsoService {

    /** 注册,用户名重复抛统一冲突异常 */
    void register(RegisterDTO registerDTO);

    /** 登录,成功返回 token,失败抛统一异常 */
    String login(LoginDTO loginDTO);
}
