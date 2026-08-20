package com.example.study11.filter;

import com.example.study11.config.LoginConfig;
import com.example.study11.exception.ApiException;
import com.example.study11.utils.AESUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenInterceptorTest {

    private static final String AES_KEY = "0000000000000000";

    @InjectMocks
    private TokenInterceptor tokenInterceptor;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private LoginConfig loginConfig;

    @Test
    void shouldAllowRequestWithValidTokenAndRedisSession() throws Exception {
        long timestamp = System.currentTimeMillis();
        String token = AESUtil.encrypt("7:" + timestamp, AES_KEY);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-token", token);

        when(loginConfig.getSecret()).thenReturn(AES_KEY);
        when(loginConfig.getTokenExpireSeconds()).thenReturn(1800L);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:7")).thenReturn("7");

        boolean allowed = tokenInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(allowed);
        verify(valueOperations).get("user:7");
    }

    @Test
    void shouldRejectExpiredToken() {
        long expiredTimestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(31);
        String token = AESUtil.encrypt("7:" + expiredTimestamp, AES_KEY);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-token", token);

        when(loginConfig.getSecret()).thenReturn(AES_KEY);
        when(loginConfig.getTokenExpireSeconds()).thenReturn(1800L);

        ApiException exception = assertThrows(ApiException.class,
                () -> tokenInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}
