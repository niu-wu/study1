package com.example.study11.service.impl;

import com.example.study11.config.PasswordConfig;
import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.ChangePasswordDTO;
import com.example.study11.entity.po.UserPo;
import com.example.study11.exception.ApiException;
import com.example.study11.service.CaptchaService;
import com.example.study11.service.NotificationService;
import com.example.study11.utils.PasswordPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 修改密码业务边界测试。
 *
 * <p>测试通过 Redis 中的失败次数和成功次数驱动服务，重点确认阈值判断发生在数据库更新之前。</p>
 */
@ExtendWith(MockitoExtension.class)
class PasswordChangeServiceImplTest {

    private static final Integer USER_ID = 7;
    private static final String CLIENT_IP = "192.0.2.10";
    private static final String OLD_PASSWORD = "OldPass1";
    private static final String NEW_PASSWORD = "Bb234567";
    private static final String STORED_PASSWORD = "$2a$stored-password";
    private static final String ENCODED_NEW_PASSWORD = "$2a$new-password";
    private static final String CAPTCHA_ID = "captcha-1";
    private static final String CAPTCHA_CODE = "ABCD";

    @InjectMocks
    private PasswordChangeServiceImpl passwordChangeService;

    @Mock
    private UserDao userDao;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PasswordConfig passwordConfig;

    @Mock
    private PasswordPolicy passwordPolicy;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(passwordConfig.getMaxWrongAttempts()).thenReturn(6);
        lenient().when(passwordConfig.getCaptchaThreshold()).thenReturn(3);
        lenient().when(passwordConfig.getMaxDailyChanges()).thenReturn(3);
        lenient().when(userDao.selectUserById(USER_ID)).thenReturn(existingUser());
        lenient().when(userDao.updatePasswordById(any(UserPo.class))).thenReturn(1);
        lenient().when(passwordPolicy.isValid(NEW_PASSWORD)).thenReturn(true);
        lenient().when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);
        // 失败次数和成功次数由每个测试按边界值单独设置。
        lenient().when(valueOperations.increment(anyString(), eq(1L))).thenReturn(1L);
    }

    @ParameterizedTest(name = "已有错误次数 {0} 时允许第 {1} 次尝试")
    @CsvSource({"0,1", "1,2", "2,3"})
    void 前三次旧密码错误不要求图形验证码(int wrongCount, int attempt) {
        // 将每日上限临时调高，避免本测试被每日次数规则遮蔽。
        when(passwordConfig.getMaxDailyChanges()).thenReturn(99);
        stubRedisCount(wrongCount);
        when(passwordEncoder.matches(OLD_PASSWORD, STORED_PASSWORD)).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class,
                () -> passwordChangeService.changePassword(USER_ID, request(true), CLIENT_IP));

        assertNotNull(exception, "第 " + attempt + " 次错误应返回业务异常");
        verify(captchaService, never()).validate(any(), anyString(), anyString());
        verify(valueOperations).increment(anyString(), eq(1L));
        verify(userDao, never()).updatePasswordById(any(UserPo.class));
        verify(notificationService, never()).sendPasswordChanged(any(UserPo.class), anyString(), any(LocalDateTime.class));
    }

    @ParameterizedTest(name = "已有错误次数 {0} 时校验第 {1} 次验证码")
    @CsvSource({"3,4", "4,5", "5,6"})
    void 第四至第六次旧密码错误必须先通过图形验证码(int wrongCount, int attempt) {
        // 每日次数上限调高，确保断言针对旧密码错误次数，而不是每日上限。
        when(passwordConfig.getMaxDailyChanges()).thenReturn(99);
        stubRedisCount(wrongCount);
        when(captchaService.validate(USER_ID, CAPTCHA_ID, CAPTCHA_CODE)).thenReturn(true);
        when(passwordEncoder.matches(OLD_PASSWORD, STORED_PASSWORD)).thenReturn(false);

        ChangePasswordDTO request = request(true);
        request.setCaptchaId(CAPTCHA_ID);
        request.setCaptchaCode(CAPTCHA_CODE);

        ApiException exception = assertThrows(ApiException.class,
                () -> passwordChangeService.changePassword(USER_ID, request, CLIENT_IP));

        assertNotNull(exception, "第 " + attempt + " 次错误仍应返回密码错误异常");
        verify(captchaService).validate(USER_ID, CAPTCHA_ID, CAPTCHA_CODE);
        verify(valueOperations).increment(anyString(), eq(1L));
        verify(userDao, never()).updatePasswordById(any(UserPo.class));
        verify(notificationService, never()).sendPasswordChanged(any(UserPo.class), anyString(), any(LocalDateTime.class));
    }

    @Test
    void 第七次提交直接禁止且不再校验验证码或旧密码() {
        // 失败次数已经达到 6 次；第七次必须在任何密码校验和数据库操作之前被拒绝。
        when(passwordConfig.getMaxDailyChanges()).thenReturn(99);
        stubRedisCount(6);

        assertThrows(ApiException.class,
                () -> passwordChangeService.changePassword(USER_ID, request(true), CLIENT_IP));

        verify(captchaService, never()).validate(any(), anyString(), anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userDao, never()).updatePasswordById(any(UserPo.class));
        verify(notificationService, never()).sendPasswordChanged(any(UserPo.class), anyString(), any(LocalDateTime.class));
    }

    @Test
    void 当日已经成功修改三次时拒绝第四次修改() {
        // Redis 中的成功次数达到上限；请求不得触发密码更新或邮件通知。
        lenient().when(passwordConfig.getMaxDailyChanges()).thenReturn(3);
        lenient().when(passwordConfig.getMaxWrongAttempts()).thenReturn(99);
        stubRedisCount(3);
        lenient().when(captchaService.validate(USER_ID, CAPTCHA_ID, CAPTCHA_CODE)).thenReturn(true);
        lenient().when(passwordEncoder.matches(OLD_PASSWORD, STORED_PASSWORD)).thenReturn(true);

        ChangePasswordDTO request = request(true);
        request.setCaptchaId(CAPTCHA_ID);
        request.setCaptchaCode(CAPTCHA_CODE);

        assertThrows(ApiException.class,
                () -> passwordChangeService.changePassword(USER_ID, request, CLIENT_IP));

        verify(userDao, never()).updatePasswordById(any(UserPo.class));
        verify(notificationService, never()).sendPasswordChanged(any(UserPo.class), anyString(), any(LocalDateTime.class));
    }

    @Test
    void 未进行最终确认时拒绝修改且不发送通知() {
        // confirmed=false 表示用户取消了最终确认，数据库和通知都不能产生副作用。
        lenient().when(passwordConfig.getMaxDailyChanges()).thenReturn(3);
        stubRedisCount(0);
        lenient().when(passwordEncoder.matches(OLD_PASSWORD, STORED_PASSWORD)).thenReturn(true);

        assertThrows(ApiException.class,
                () -> passwordChangeService.changePassword(USER_ID, request(false), CLIENT_IP));

        verify(userDao, never()).updatePasswordById(any(UserPo.class));
        verify(notificationService, never()).sendPasswordChanged(any(UserPo.class), anyString(), any(LocalDateTime.class));
    }

    @Test
    void 修改成功后只更新密码并发送包含IP和时间的通知() {
        // 当前错误次数为 2、成功次数为 2，满足两项上限，应该可以完成第三次成功修改。
        stubRedisCount(2);
        when(passwordEncoder.matches(OLD_PASSWORD, STORED_PASSWORD)).thenReturn(true);

        assertDoesNotThrow(() -> passwordChangeService.changePassword(USER_ID, request(true), CLIENT_IP));

        ArgumentCaptor<UserPo> userCaptor = ArgumentCaptor.forClass(UserPo.class);
        verify(userDao).updatePasswordById(userCaptor.capture());
        UserPo updatedUser = userCaptor.getValue();
        assertEquals(USER_ID, updatedUser.getId());
        assertEquals(ENCODED_NEW_PASSWORD, updatedUser.getPassword());
        assertNotNull(updatedUser.getUpdatetime());

        verify(notificationService).sendPasswordChanged(
                eq(existingUser()), eq(CLIENT_IP), any(LocalDateTime.class));
        verify(valueOperations, atLeastOnce()).increment(anyString(), eq(1L));
    }

    @Test
    void 数据库没有更新记录时不应增加成功次数或发送通知() {
        stubRedisCount(0);
        when(passwordEncoder.matches(OLD_PASSWORD, STORED_PASSWORD)).thenReturn(true);
        when(userDao.updatePasswordById(any(UserPo.class))).thenReturn(0);

        assertThrows(ApiException.class,
                () -> passwordChangeService.changePassword(USER_ID, request(true), CLIENT_IP));

        verify(notificationService, never()).sendPasswordChanged(any(UserPo.class), anyString(), any(LocalDateTime.class));
        verify(valueOperations, never()).increment(anyString(), eq(1L));
    }

    @Test
    void BCrypt摘要文本不能作为旧密码明文通过校验() {
        stubRedisCount(0);
        ChangePasswordDTO request = request(true);
        request.setOldPassword(STORED_PASSWORD);
        when(passwordEncoder.matches(STORED_PASSWORD, STORED_PASSWORD)).thenReturn(false);

        assertThrows(ApiException.class,
                () -> passwordChangeService.changePassword(USER_ID, request, CLIENT_IP));

        verify(valueOperations).increment(anyString(), eq(1L));
        verify(userDao, never()).updatePasswordById(any(UserPo.class));
    }

    /**
     * 用同一个值响应 Redis 读取，令每个边界测试只关注正在验证的阈值。
     * 具体 key 名称属于生产实现细节，不在测试中固化。
     */
    private void stubRedisCount(int count) {
        lenient().when(valueOperations.get(anyString())).thenReturn(String.valueOf(count));
    }

    private ChangePasswordDTO request(boolean confirmed) {
        ChangePasswordDTO request = new ChangePasswordDTO();
        request.setOldPassword(OLD_PASSWORD);
        request.setNewPassword(NEW_PASSWORD);
        request.setConfirmPassword(NEW_PASSWORD);
        request.setConfirmed(confirmed);
        return request;
    }

    private UserPo existingUser() {
        UserPo user = new UserPo();
        user.setId(USER_ID);
        user.setUsername("test-user");
        user.setPassword(STORED_PASSWORD);
        user.setEmail("test@example.com");
        user.setIsDeleted(0);
        return user;
    }
}
