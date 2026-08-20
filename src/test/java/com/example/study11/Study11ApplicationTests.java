package com.example.study11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 基础应用上下文测试，确认启动入口可以被 Spring Boot 正常加载
 */
@SpringBootTest
class Study11ApplicationTests {

    @Test
    @DisplayName("应用上下文可以正常启动")
    void contextLoads() {
    }

}
