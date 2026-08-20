package com.example.study11.service.impl;

import com.example.study11.config.PasswordConfig;
import com.example.study11.entity.vo.CaptchaVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.CaptchaService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed 图形验证码实现。
 *
 * <p>验证码编号同时绑定用户 ID 和 Redis 短期键，校验成功后立即删除，
 * 避免验证码跨用户复用或重复提交。</p>
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final String KEY_PREFIX = "password:captcha:";
    private static final String CAPTCHA_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 4;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 42;
    private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then "
                    + "return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final SecureRandom secureRandom = new SecureRandom();

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PasswordConfig passwordConfig;

    @Override
    public CaptchaVO generate(Integer userId) {
        if (userId == null || userId <= 0) {
            throw ApiException.unauthorized("未登录");
        }
        String captchaId = UUID.randomUUID().toString();
        String code = randomCode();
        long expireSeconds = configuredExpireSeconds();
        try {
            stringRedisTemplate.opsForValue().set(
                    captchaKey(userId, captchaId), code, expireSeconds, TimeUnit.SECONDS);
            return new CaptchaVO(captchaId, renderImage(code), expireSeconds);
        } catch (RuntimeException e) {
            log.error("生成图形验证码失败: userId={}", userId, e);
            throw new ApiException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "验证码服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public boolean validate(Integer userId, String captchaId, String captchaCode) {
        if (userId == null || userId <= 0
                || captchaId == null || captchaId.isBlank()
                || captchaCode == null || captchaCode.isBlank()) {
            return false;
        }
        try {
            String key = captchaKey(userId, captchaId);
            String normalizedCode = captchaCode.trim().toUpperCase(Locale.ROOT);
            // 真实 Redis 用脚本完成比较和删除，保证验证码只能被成功消费一次
            Long consumed = stringRedisTemplate.execute(CONSUME_SCRIPT,
                    List.of(key), normalizedCode);
            if (consumed != null) {
                return consumed == 1L;
            }

            // 兼容不支持脚本的测试/旧实现，退回 GET + DELETE
            String expected = stringRedisTemplate.opsForValue().get(key);
            if (expected == null || !expected.equalsIgnoreCase(normalizedCode)) {
                return false;
            }
            // 只有答案正确才消费，输错时允许用户在有效期内继续输入
            stringRedisTemplate.delete(key);
            return true;
        } catch (RuntimeException e) {
            // Redis 故障时返回 false，确保不会因基础设施异常绕过验证码
            log.error("校验图形验证码失败: userId={}", userId, e);
            return false;
        }
    }

    /** 生成不含容易混淆字符的验证码文本 */
    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CAPTCHA_ALPHABET.charAt(
                    secureRandom.nextInt(CAPTCHA_ALPHABET.length())));
        }
        return code.toString();
    }

    /** 将验证码绘制成 PNG data URI，便于前端直接展示 */
    private String renderImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 247, 250));
            graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
            graphics.setColor(new Color(210, 218, 230));
            for (int i = 0; i < 5; i++) {
                int x = secureRandom.nextInt(IMAGE_WIDTH);
                int y = secureRandom.nextInt(IMAGE_HEIGHT);
                graphics.drawLine(x, y,
                        secureRandom.nextInt(IMAGE_WIDTH), secureRandom.nextInt(IMAGE_HEIGHT));
            }
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(new Color(40 + secureRandom.nextInt(100),
                        60 + secureRandom.nextInt(100), 100 + secureRandom.nextInt(100)));
                graphics.drawString(String.valueOf(code.charAt(i)), 12 + i * 26, 29);
            }
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                ImageIO.write(image, "png", output);
                return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException("验证码图片编码失败", e);
            }
        } finally {
            graphics.dispose();
        }
    }

    private String captchaKey(Integer userId, String captchaId) {
        return KEY_PREFIX + userId + ":" + captchaId;
    }

    private long configuredExpireSeconds() {
        long configured = passwordConfig == null ? 0 : passwordConfig.getCaptchaExpireSeconds();
        return configured > 0 ? configured : 300;
    }
}
