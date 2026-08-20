package com.example.study11.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 修改密码规则测试。
 *
 * <p>需求约束：密码必须正好 8 位，并且同时包含大写字母、小写字母和数字。
 * 规则只检查字符类别，不要求密码中出现字面量 {@code Aa1}，也不要求特殊符号。</p>
 */
class PasswordPolicyTest {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @Test
    void 八位且包含大小写字母和数字时校验通过() {
        // 不包含固定的 Aa1，但满足类别要求，说明规则不是按固定字符串匹配。
        assertTrue(passwordPolicy.isValid("Bb234567"));
    }

    @Test
    void 少于八位时校验失败() {
        assertFalse(passwordPolicy.isValid("Bb23456"));
    }

    @Test
    void 多于八位时校验失败() {
        assertFalse(passwordPolicy.isValid("Bb2345678"));
    }

    @Test
    void 缺少大写字母时校验失败() {
        assertFalse(passwordPolicy.isValid("bb234567"));
    }

    @Test
    void 缺少小写字母时校验失败() {
        assertFalse(passwordPolicy.isValid("BB234567"));
    }

    @Test
    void 缺少数字时校验失败() {
        assertFalse(passwordPolicy.isValid("BbCdefgh"));
    }
}
