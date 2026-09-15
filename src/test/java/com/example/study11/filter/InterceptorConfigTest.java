package com.example.study11.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class InterceptorConfigTest {

    @Test
    void keepsEveryResumeRouteBehindTokenInterceptor() {
        TokenInterceptor tokenInterceptor = mock(TokenInterceptor.class);
        InterceptorConfig interceptorConfig = new InterceptorConfig();
        ReflectionTestUtils.setField(interceptorConfig, "tokenInterceptor", tokenInterceptor);
        ExposedInterceptorRegistry registry = new ExposedInterceptorRegistry();

        interceptorConfig.addInterceptors(registry);

        List<Object> interceptors = registry.exposedInterceptors();
        assertEquals(1, interceptors.size());
        MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptors.get(0);
        assertSame(tokenInterceptor, mappedInterceptor.getInterceptor());
        assertTrue(mappedInterceptor.matches(request("/api/resumes/upload")));
        assertTrue(mappedInterceptor.matches(request("/api/resumes/42")));
        assertTrue(mappedInterceptor.matches(request("/api/resumes/download/42")));
        assertTrue(mappedInterceptor.matches(request("/api/employee-archives/page")));
        assertTrue(mappedInterceptor.matches(request("/api/employee-archives/statistics")));
        assertTrue(mappedInterceptor.matches(request("/api/employee-archives/7c9e6679-7425-40de-944b-e07fc1f90ae7")));
        assertTrue(mappedInterceptor.matches(request("/api/employee-archives/7c9e6679-7425-40de-944b-e07fc1f90ae7/photo")));
        assertTrue(mappedInterceptor.matches(request("/api/employee-archives/7c9e6679-7425-40de-944b-e07fc1f90ae7/salaries")));
        assertTrue(mappedInterceptor.matches(request("/api/employee-archives/7c9e6679-7425-40de-944b-e07fc1f90ae7/assignments")));
        assertTrue(mappedInterceptor.matches(request("/api/employee-archives/7c9e6679-7425-40de-944b-e07fc1f90ae7/accounts")));
        assertFalse(mappedInterceptor.matches(request("/sso/login")));
        assertFalse(mappedInterceptor.matches(request("/sso/register")));
    }

    private static MockHttpServletRequest request(String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        ServletRequestPathUtils.parseAndCache(request);
        return request;
    }

    private static final class ExposedInterceptorRegistry extends InterceptorRegistry {

        private List<Object> exposedInterceptors() {
            return getInterceptors();
        }
    }
}
