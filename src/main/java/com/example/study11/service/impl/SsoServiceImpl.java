package com.example.study11.service.impl;

import com.example.study11.config.LoginConfig;
import com.example.study11.entity.dto.LoginDTO;
import com.example.study11.entity.dto.RegisterDTO;
import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.po.UserPo;
import com.example.study11.exception.ApiException;
import com.example.study11.service.SsoService;
import com.example.study11.service.UserService;
import com.example.study11.utils.AESUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 登录注册业务实现
 */
@Slf4j
@Service
public class SsoServiceImpl implements SsoService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private LoginConfig loginConfig;

    /**
     * BCrypt 编码器。使用可选注入是为了兼容旧的单元测试和迁移期无安全组件的环境
     */
    @Autowired(required = false)
    private PasswordEncoder passwordEncoder;

    @Override
    public void register(RegisterDTO registerDTO) {
        // 复用 UserService 创建逻辑:已含用户名唯一校验 + 插入 + 主键回填
        UserSaveDTO userSaveDTO = new UserSaveDTO();
        userSaveDTO.setUsername(registerDTO.getUsername());
        userSaveDTO.setPassword(registerDTO.getPassword());
        userSaveDTO.setEmail(registerDTO.getEmail());
        userService.dealSave(userSaveDTO);
        log.info("注册成功: {}", registerDTO.getUsername());
    }

    @Override
    public String login(LoginDTO loginDTO) {
        // 用户不存在/已删除/密码不匹配,统一返回"用户名或密码错误",避免暴露用户是否存在
        UserPo userPo = userService.getByUserName(loginDTO.getUsername());
        if (userPo == null
                || (userPo.getIsDeleted() != null && userPo.getIsDeleted() != 0)
                || !passwordMatches(loginDTO.getPassword(), userPo.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户名或密码错误");
        }
        // Token 明文只包含用户ID和签发时间戳，生成后再进行 AES 加密
        long timestamp = System.currentTimeMillis();
        String tokenContent = userPo.getId() + ":" + timestamp;
        String token = AESUtil.encrypt(tokenContent, loginConfig.getSecret());

        // Redis 会话仍按用户ID保存，并按配置在 30 分钟后自动过期
        stringRedisTemplate.opsForValue().set(
                "user:" + userPo.getId(),
                userPo.getId().toString(),
                loginConfig.getTokenExpireSeconds(),
                TimeUnit.SECONDS);
        log.info("登录成功: {}", userPo.getUsername());
        return token;
    }

    /**
     * 新用户使用 BCrypt 校验，历史明文账号在迁移完成前仍可正常登录
     */
    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (looksLikeBcrypt(storedPassword)) {
            // BCrypt 摘要不允许回退到明文比较，防止摘要泄露后被当作密码提交
            if (passwordEncoder == null) {
                return false;
            }
            try {
                return passwordEncoder.matches(rawPassword, storedPassword);
            } catch (IllegalArgumentException e) {
                log.debug("密码摘要格式无法由 BCrypt 解析，拒绝本次登录");
                return false;
            }
        }
        return Objects.equals(rawPassword, storedPassword);
    }

    /** 只把明确标记为 BCrypt 的摘要交给编码器，避免摘要字符串被当作旧明文接受。 */
    private boolean looksLikeBcrypt(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }
}
