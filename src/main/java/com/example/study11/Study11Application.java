package com.example.study11;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * study1-1 基础工程启动入口
 * Mapper 扫描见 {@link com.example.study11.config.MyBatisConfig}
 */
@SpringBootApplication
public class Study11Application {

    /**
     * 启动 Spring Boot 应用
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Study11Application.class, args);
    }

}
