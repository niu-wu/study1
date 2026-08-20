package com.example.study11.service.impl;

import com.example.study11.config.LoginConfig;
import com.example.study11.entity.dto.LoginDTO;
import com.example.study11.entity.po.UserPo;
import com.example.study11.service.UserService;
import com.example.study11.utils.AESUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SsoServiceImplTest {

    private static final String AES_KEY = "0000000000000000";

    @InjectMocks
    private SsoServiceImpl ssoService;

    @Mock
    private UserService userService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private LoginConfig loginConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldCreateTimestampTokenAndStoreThirtyMinuteSession() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test-user");
        loginDTO.setPassword("123456");

        UserPo userPo = new UserPo();
        userPo.setId(7);
        userPo.setUsername("test-user");
        userPo.setPassword("123456");
        userPo.setIsDeleted(0);

        when(userService.getByUserName("test-user")).thenReturn(userPo);
        when(loginConfig.getSecret()).thenReturn(AES_KEY);
        when(loginConfig.getTokenExpireSeconds()).thenReturn(1800L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = ssoService.login(loginDTO);
        String[] tokenParts = AESUtil.decrypt(token, AES_KEY).split(":", 2);

        assertEquals("7", tokenParts[0]);
        assertTrue(Long.parseLong(tokenParts[1]) > 0);
        verify(valueOperations).set("user:7", "7", 1800L, TimeUnit.SECONDS);
    }

    @Test
    void shouldLoginWithBcryptPasswordAfterPasswordChange() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("test-user");
        loginDTO.setPassword("Bb234567");

        UserPo userPo = new UserPo();
        userPo.setId(7);
        userPo.setUsername("test-user");
        userPo.setPassword("$2a$10$encoded-password");
        userPo.setIsDeleted(0);

        when(userService.getByUserName("test-user")).thenReturn(userPo);
        when(passwordEncoder.matches("Bb234567", userPo.getPassword())).thenReturn(true);
        when(loginConfig.getSecret()).thenReturn(AES_KEY);
        when(loginConfig.getTokenExpireSeconds()).thenReturn(1800L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        String token = ssoService.login(loginDTO);

        assertTrue(token != null && !token.isBlank());
        verify(passwordEncoder).matches("Bb234567", userPo.getPassword());
    }
}
