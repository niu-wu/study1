package com.example.study11.controller;

import com.example.study11.entity.dto.ChangePasswordDTO;
import com.example.study11.entity.vo.CaptchaVO;
import com.example.study11.exception.ApiException;
import com.example.study11.filter.TokenInterceptor;
import com.example.study11.service.CaptchaService;
import com.example.study11.service.PasswordChangeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户的密码安全接口。
 *
 * <p>控制器只负责提取已由拦截器验证的用户 ID 和客户端 IP，数据库访问全部下沉到业务层。</p>
 */
@RestController
public class PasswordController {

    @Resource
    private CaptchaService captchaService;

    @Resource
    private PasswordChangeService passwordChangeService;

    /**
     * 获取当前用户的图形验证码。
     */
    @GetMapping("users/me/password/captcha")
    public ResponseEntity<CaptchaVO> captcha(HttpServletRequest request) {
        Integer userId = currentUserId(request);
        return ResponseEntity.ok(captchaService.generate(userId));
    }

    /**
     * 修改当前登录用户密码。
     */
    @PutMapping("users/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Validated ChangePasswordDTO changePasswordDTO,
                                                HttpServletRequest request) {
        Integer userId = currentUserId(request);
        // 只使用 Servlet 容器提供的远端地址，不无条件信任可被客户端伪造的转发头。
        String clientIp = request.getRemoteAddr();
        passwordChangeService.changePassword(userId, changePasswordDTO, clientIp);
        return ResponseEntity.ok().build();
    }

    /** 从拦截器写入的请求属性读取身份，缺失时按未登录处理。 */
    private Integer currentUserId(HttpServletRequest request) {
        Object value = request.getAttribute(TokenInterceptor.CURRENT_USER_ID_ATTRIBUTE);
        if (value instanceof Integer userId && userId > 0) {
            return userId;
        }
        throw ApiException.unauthorized("未登录");
    }
}
