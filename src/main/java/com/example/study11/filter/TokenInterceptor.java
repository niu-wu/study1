package com.example.study11.filter;

import com.example.study11.config.LoginConfig;
import com.example.study11.exception.ApiException;
import com.example.study11.utils.AESUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * Token 拦截器
 * http > filter > dispatch > interceptor > aop > controller
 */
@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    /**
     * 当前登录用户 ID 在请求范围内的属性名，Controller 只能从这里取得身份。
     */
    public static final String CURRENT_USER_ID_ATTRIBUTE =
            "com.example.study11.currentUserId";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private LoginConfig loginConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 从 Header 获取 Token
        String token = request.getHeader("x-token");
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        try {
            // 解密后的格式为：用户ID:签发时间戳
            String decrypt = AESUtil.decrypt(token, loginConfig.getSecret());
            String[] tokenParts = decrypt.split(":", 2);
            if (tokenParts.length != 2 || tokenParts[0].isBlank() || tokenParts[1].isBlank()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "未登录");
            }

            Integer userId = Integer.valueOf(tokenParts[0]);
            long issuedAt = Long.parseLong(tokenParts[1]);
            long now = System.currentTimeMillis();
            long expireMillis = TimeUnit.SECONDS.toMillis(loginConfig.getTokenExpireSeconds());

            // 拒绝未来签发的 token，以及超过配置有效期的 token。
            if (userId <= 0 || issuedAt <= 0 || issuedAt > now || now - issuedAt > expireMillis) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "用户登录会话超时，请重新登录");
            }

            // Redis key 必须与登录时写入的 key 保持一致。
            String userKey = "user:" + userId;
            String strJson = stringRedisTemplate.opsForValue().get(userKey);
            if (!String.valueOf(userId).equals(strJson)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "用户登录会话超时，请重新登录");
            }
            // 身份由已验证的 token 产生，避免 Controller 信任请求体或 URL 中的用户 ID。
            request.setAttribute(CURRENT_USER_ID_ATTRIBUTE, userId);
            return true;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            // 不记录完整 token，避免敏感认证信息进入日志。
            log.error("token解析失败，非法操作", e);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "未登录");
        }
    }

    /** 请求结束后清理属性，避免异步复用请求对象时残留身份信息。 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        request.removeAttribute(CURRENT_USER_ID_ATTRIBUTE);
    }
}
