package com.example.study11.service.impl;

import com.example.study11.config.PasswordConfig;
import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.ChangePasswordDTO;
import com.example.study11.entity.po.UserPo;
import com.example.study11.exception.ApiException;
import com.example.study11.service.CaptchaService;
import com.example.study11.service.NotificationService;
import com.example.study11.service.PasswordChangeService;
import com.example.study11.utils.PasswordPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 修改密码业务实现
 *
 * <p>所有数据库操作都通过 {@link UserDao} 完成。Redis 只保存每日计数和验证码，
 * 不保存密码。业务流程先完成所有校验，只有旧密码正确且最终确认后才更新数据库。</p>
 */
@Slf4j
@Service
public class PasswordChangeServiceImpl implements PasswordChangeService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final String WRONG_COUNT_PREFIX = "password:change:wrong:";
    private static final String SUCCESS_COUNT_PREFIX = "password:change:success:";
    private static final String LOCK_PREFIX = "password:change:lock:";
    /** SMTP/数据库异常时也给流程留出余量，邮件超时配置会进一步限制临界区时长。 */
    private static final long LOCK_EXPIRE_SECONDS = 300L;
    private static final DefaultRedisScript<Long> ACQUIRE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "return redis.call('set', KEYS[1], ARGV[1], 'NX', 'EX', ARGV[2]) and 1 or 0",
            Long.class);
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);
    private static final DefaultRedisScript<Long> INCREMENT_COUNTER_SCRIPT = new DefaultRedisScript<>(
            "local value = redis.call('incr', KEYS[1]); "
                    + "if value == 1 then redis.call('expire', KEYS[1], ARGV[1]); end; "
                    + "return value",
            Long.class);

    private static final String CODE_CONFIRM_REQUIRED = "PASSWORD_CONFIRM_REQUIRED";
    private static final String CODE_POLICY_INVALID = "PASSWORD_POLICY_INVALID";
    private static final String CODE_DAILY_LIMIT = "PASSWORD_DAILY_LIMIT";
    private static final String CODE_WRONG_LIMIT = "PASSWORD_WRONG_LIMIT";
    private static final String CODE_CAPTCHA_INVALID = "PASSWORD_CAPTCHA_INVALID";
    private static final String CODE_REDIS_UNAVAILABLE = "PASSWORD_REDIS_UNAVAILABLE";

    private final UserDao userDao;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordConfig passwordConfig;
    private final PasswordPolicy passwordPolicy;
    private final CaptchaService captchaService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建修改密码业务对象
     */
    public PasswordChangeServiceImpl(UserDao userDao,
                                     StringRedisTemplate stringRedisTemplate,
                                     PasswordConfig passwordConfig,
                                     PasswordPolicy passwordPolicy,
                                     CaptchaService captchaService,
                                     NotificationService notificationService,
                                     PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.stringRedisTemplate = stringRedisTemplate;
        this.passwordConfig = passwordConfig;
        this.passwordPolicy = passwordPolicy;
        this.captchaService = captchaService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 固定数量的锁条带，避免按用户 ID 无限增长的锁表造成长期内存泄漏
     * Redis 计数仍是跨实例的最终计数来源；集群部署应在网关或 Redis 层配置互斥策略
     */
    private static final Object[] USER_LOCK_STRIPES = createLockStripes();

    @Override
    public void changePassword(Integer userId, ChangePasswordDTO request, String clientIp) {
        if (userId == null || userId <= 0) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        if (request == null) {
            throw ApiException.badRequest("修改密码参数不能为空");
        }

        Object lock = USER_LOCK_STRIPES[Math.floorMod(userId, USER_LOCK_STRIPES.length)];
        synchronized (lock) {
            runWithRedisUserLock(userId, request, clientIp);
        }
    }

    /**
     * 使用 Redis NX 锁覆盖完整业务流程，避免集群节点同时读到同一个旧计数
     * 单元测试中的 mock 若返回 null，则退回固定锁；真实 Redis 会返回明确的 true/false
     */
    private void runWithRedisUserLock(Integer userId, ChangePasswordDTO request, String clientIp) {
        String lockKey = LOCK_PREFIX + userId;
        String lockToken = UUID.randomUUID().toString();
        boolean redisLockAcquired = false;
        try {
            Long acquired = stringRedisTemplate.execute(ACQUIRE_LOCK_SCRIPT,
                    List.of(lockKey), lockToken, String.valueOf(LOCK_EXPIRE_SECONDS));
            if (Long.valueOf(0L).equals(acquired)) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "修改密码请求正在处理中，请稍后再试",
                        Map.of("code", "PASSWORD_CHANGE_IN_PROGRESS"));
            }
            // null 仅作为测试/兼容实现的本机锁回退；真实 Redis 脚本会返回 1 或 0
            redisLockAcquired = Long.valueOf(1L).equals(acquired);
            changePasswordLocked(userId, request, clientIp);
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("获取密码修改 Redis 锁失败: userId={}", userId, e);
            throw businessException(HttpStatus.SERVICE_UNAVAILABLE,
                    "密码修改服务暂时不可用，请稍后再试", CODE_REDIS_UNAVAILABLE);
        } finally {
            if (redisLockAcquired) {
                releaseRedisUserLock(lockKey, lockToken);
            }
        }
    }

    /** 仅删除仍属于本次请求的锁，避免误删其他请求续期后的锁。 */
    private void releaseRedisUserLock(String lockKey, String lockToken) {
        try {
            stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT,
                    List.of(lockKey), lockToken);
        } catch (RuntimeException e) {
            // 锁带有短 TTL，释放失败不会影响本次已经完成的结果
            log.warn("释放密码修改 Redis 锁失败: key={}", lockKey, e);
        }
    }

    /**
     * 在用户锁内执行修改流程，确保计数检查、密码校验和计数更新顺序稳定
     */
    private void changePasswordLocked(Integer userId, ChangePasswordDTO request, String clientIp) {
        // 二次验证是最后一次确认，后端不能只依赖前端弹窗状态。
        if (!Boolean.TRUE.equals(request.getConfirmed())) {
            throw businessException(HttpStatus.BAD_REQUEST, "未确认修改密码", CODE_CONFIRM_REQUIRED);
        }

        // 新密码格式和两次输入一致性在任何计数变更前校验，避免无效请求消耗次数
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw businessException(HttpStatus.BAD_REQUEST, "两次输入的新密码不一致", CODE_POLICY_INVALID);
        }
        if (passwordPolicy == null || !passwordPolicy.isValid(request.getNewPassword())) {
            throw businessException(HttpStatus.BAD_REQUEST,
                    "密码必须正好8位且包含大写字母、小写字母和数字", CODE_POLICY_INVALID);
        }

        UserPo user = userDao.selectUserById(userId);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            throw ApiException.notFound("用户不存在");
        }

        String date = LocalDate.now(BUSINESS_ZONE).toString();
        String wrongKey = WRONG_COUNT_PREFIX + userId + ":" + date;
        String successKey = SUCCESS_COUNT_PREFIX + userId + ":" + date;
        int maxDailyChanges = positiveOrDefault(passwordConfig == null
                ? 0 : passwordConfig.getMaxDailyChanges(), 3);
        int maxWrongAttempts = positiveOrDefault(passwordConfig == null
                ? 0 : passwordConfig.getMaxWrongAttempts(), 6);
        int captchaThreshold = nonNegativeOrDefault(passwordConfig == null
                ? 0 : passwordConfig.getCaptchaThreshold(), 3);

        // 每日成功次数达到上限时，不再继续校验密码，避免无意义地暴露校验结果
        int successCount = readCounter(successKey);
        if (successCount >= maxDailyChanges) {
            throw businessException(HttpStatus.TOO_MANY_REQUESTS,
                    "今日密码修改次数已达上限，请明日再试", CODE_DAILY_LIMIT);
        }

        int wrongCount = readCounter(wrongKey);
        // count=6 表示前六次已经用完，因此第七次请求必须直接拒绝
        if (wrongCount >= maxWrongAttempts) {
            throw businessException(HttpStatus.FORBIDDEN,
                    "旧密码错误次数已达上限，请明日再试", CODE_WRONG_LIMIT);
        }

        // 已有三次失败记录时，当前请求就是第 4 次，必须先通过图形验证码
        if (wrongCount >= captchaThreshold) {
            boolean captchaValid = captchaService != null
                    && captchaService.validate(userId, request.getCaptchaId(), request.getCaptchaCode());
            if (!captchaValid) {
                throw businessException(HttpStatus.BAD_REQUEST,
                        "图形验证码错误或已过期", CODE_CAPTCHA_INVALID);
            }
        }

        if (!passwordMatches(request.getOldPassword(), user.getPassword())) {
            int currentCount = incrementCounter(wrongKey, wrongCount);
            String message = currentCount >= maxWrongAttempts
                    ? "旧密码错误，已用完今日次数；请明日再试"
                    : "旧密码错误";
            throw businessException(HttpStatus.BAD_REQUEST, message,
                    currentCount >= maxWrongAttempts ? CODE_WRONG_LIMIT : "OLD_PASSWORD_INVALID");
        }

        // 通知只能接收修改前快照，避免把新密码或变更后的字段误传给通知实现
        UserPo notificationUser = copyUser(user);
        String encodedPassword = encodePassword(request.getNewPassword());
        user.setPassword(encodedPassword);
        user.setUpdatetime(new Date());
        int affectedRows = userDao.updatePasswordById(user);
        if (affectedRows != 1) {
            // 用户可能在校验后被删除或并发修改；此时不能把本次操作记为成功。
            throw new ApiException(HttpStatus.CONFLICT, "密码修改失败，请刷新后重试",
                    Map.of("code", "PASSWORD_UPDATE_CONFLICT"));
        }

        // 数据库更新成功后再增加成功次数；旧密码错误次数按当天累计，不因成功修改而重置
        incrementCounter(successKey, successCount);

        LocalDateTime changedAt = LocalDateTime.now(BUSINESS_ZONE);
        if (notificationService != null) {
            try {
                notificationService.sendPasswordChanged(notificationUser,
                        normalizeIp(clientIp), changedAt);
            } catch (RuntimeException e) {
                // 邮件服务故障不回滚已经成功的密码修改，只记录以便后续重试或排查。
                log.error("密码已修改成功，但邮件通知发送失败: userId={}", userId, e);
            }
        }
    }

    /**
     * 兼容历史明文账号，同时优先使用 BCrypt 校验新账号
     */
    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (looksLikeBcrypt(storedPassword)) {
            // BCrypt 摘要永远不能走明文回退，即使调用方把整段摘要当作输入也必须失败
            if (passwordEncoder == null) {
                return false;
            }
            try {
                return passwordEncoder.matches(rawPassword, storedPassword);
            } catch (IllegalArgumentException e) {
                log.debug("密码摘要格式无法由 BCrypt 解析，拒绝本次校验");
                return false;
            }
        }
        return Objects.equals(rawPassword, storedPassword);
    }

    /** 只有明确的 BCrypt 摘要才交给编码器，避免把摘要文本当作历史明文密码接受。 */
    private boolean looksLikeBcrypt(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

    /**
     * 生成新密码摘要；测试或迁移场景没有编码器时保留明文兼容路径
     */
    private String encodePassword(String rawPassword) {
        if (passwordEncoder == null) {
            return rawPassword;
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 读取 Redis 计数，空值表示当天尚未产生记录，非法值视为 Redis 故障而不是绕过限制
     */
    private int readCounter(String key) {
        try {
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String value = operations.get(key);
            if (value == null || value.isBlank()) {
                return 0;
            }
            int count = Integer.parseInt(value);
            if (count < 0) {
                throw new NumberFormatException("negative counter");
            }
            return count;
        } catch (RuntimeException e) {
            if (e instanceof ApiException apiException) {
                throw apiException;
            }
            log.error("读取密码修改 Redis 计数失败: key={}", key, e);
            throw businessException(HttpStatus.SERVICE_UNAVAILABLE,
                    "密码修改服务暂时不可用，请稍后再试", CODE_REDIS_UNAVAILABLE);
        }
    }

    /**
     * 原子增加计数，并把过期时间设置到上海时区次日零点
     */
    private int incrementCounter(String key, int oldCount) {
        try {
            // 真实 Redis 使用脚本把 INCR 和首次 EXPIRE 放在同一原子操作中。
            Long scriptResult = stringRedisTemplate.execute(INCREMENT_COUNTER_SCRIPT,
                    List.of(key), String.valueOf(secondsUntilNextDay()));
            // mock/兼容 Redis 实现不支持脚本时回退到原子 INCR，保持服务可测试。
            boolean fallbackIncrement = scriptResult == null;
            Long incremented = scriptResult;
            if (fallbackIncrement) {
                incremented = stringRedisTemplate.opsForValue().increment(key, 1L);
            }
            int currentCount = incremented == null ? oldCount + 1 : Math.toIntExact(incremented);
            if (currentCount == 1 && fallbackIncrement) {
                stringRedisTemplate.expire(key, secondsUntilNextDay(), TimeUnit.SECONDS);
            }
            return currentCount;
        } catch (RuntimeException e) {
            log.error("写入密码修改 Redis 计数失败: key={}", key, e);
            throw businessException(HttpStatus.SERVICE_UNAVAILABLE,
                    "密码修改服务暂时不可用，请稍后再试", CODE_REDIS_UNAVAILABLE);
        }
    }

    /**
     * 计算当前时间到上海时区次日零点的秒数
     */
    private long secondsUntilNextDay() {
        ZonedDateTime now = ZonedDateTime.now(BUSINESS_ZONE);
        ZonedDateTime nextDay = now.toLocalDate().plusDays(1).atStartOfDay(BUSINESS_ZONE);
        return Math.max(1L, Duration.between(now, nextDay).getSeconds());
    }

    /**
     * 复制用户信息供通知使用，不复制后续写入的新密码
     */
    private UserPo copyUser(UserPo source) {
        UserPo copy = new UserPo();
        copy.setId(source.getId());
        copy.setUsername(source.getUsername());
        copy.setPassword(source.getPassword());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatetime(source.getUpdatetime());
        copy.setEmail(source.getEmail());
        copy.setPhone(source.getPhone());
        copy.setBirthday(source.getBirthday());
        copy.setStatus(source.getStatus());
        copy.setIsDeleted(source.getIsDeleted());
        return copy;
    }

    /**
     * 防止通知内容出现空 IP
     */
    private String normalizeIp(String clientIp) {
        return clientIp == null || clientIp.isBlank() ? "unknown" : clientIp;
    }

    private int positiveOrDefault(int value, int defaultValue) {
        return value > 0 ? value : defaultValue;
    }

    private int nonNegativeOrDefault(int value, int defaultValue) {
        return value >= 0 ? value : defaultValue;
    }

    private ApiException businessException(HttpStatus status, String message, String code) {
        return new ApiException(status, message, Map.of("code", code));
    }

    /** 创建固定锁条带，避免为每个用户持有永久对象。 */
    private static Object[] createLockStripes() {
        Object[] stripes = new Object[64];
        for (int i = 0; i < stripes.length; i++) {
            stripes[i] = new Object();
        }
        return stripes;
    }
}
