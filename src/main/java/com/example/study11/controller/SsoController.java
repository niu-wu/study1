package com.example.study11.controller;

import com.example.study11.entity.dto.LoginDTO;
import com.example.study11.entity.dto.RegisterDTO;
import com.example.study11.service.SsoService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录注册接口
 */
@RestController
public class SsoController {

    @Resource
    private SsoService ssoService;

    /**
     * 注册
     */
    @PostMapping("sso/register")
    public ResponseEntity<Void> register(@RequestBody @Validated RegisterDTO registerDTO) {
        ssoService.register(registerDTO);
        return ResponseEntity.ok().build();
    }

    /**
     * 登录,成功返回 token
     */
    @PostMapping("sso/login")
    public ResponseEntity<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        return ResponseEntity.ok(ssoService.login(loginDTO));
    }
}
