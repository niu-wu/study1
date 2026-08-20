package com.example.study11.utils;

import org.springframework.stereotype.Component;

/**
 * 密码格式规则。
 *
 * <p>密码必须正好 8 位，同时包含至少一个大写字母、小写字母和数字。
 * 特殊字符不是必填项，但如果使用也不会被本规则拒绝。</p>
 */
@Component
public class PasswordPolicy {

    /**
     * 判断密码是否满足修改密码的格式要求。
     *
     * @param password 待校验密码
     * @return 满足规则返回 {@code true}，空值或规则不满足返回 {@code false}
     */
    public boolean isValid(String password) {
        if (password == null || password.length() != 8) {
            return false;
        }
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char current = password.charAt(i);
            hasUppercase |= current >= 'A' && current <= 'Z';
            hasLowercase |= current >= 'a' && current <= 'z';
            hasDigit |= current >= '0' && current <= '9';
        }
        return hasUppercase && hasLowercase && hasDigit;
    }
}
